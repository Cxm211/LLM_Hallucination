  private CanInlineResult canInlineReferenceDirectly(
      Node callNode, Node fnNode) {
    if (!isDirectCallNodeReplacementPossible(fnNode)) {
      return CanInlineResult.NO;
    }

    Node block = fnNode.getLastChild();


    // CALL NODE: [ NAME, ARG1, ARG2, ... ]
    Node cArg = callNode.getFirstChild().getNext();

    // Functions called via 'call' and 'apply' have a this-object as
    // the first parameter, but this is not part of the called function's
    // parameter list.
    if (!callNode.getFirstChild().isName()) {
      if (NodeUtil.isFunctionObjectCall(callNode)) {
        // TODO(johnlenz): Support replace this with a value.
        if (cArg == null || !cArg.isThis()) {
          return CanInlineResult.NO;
        }
        cArg = cArg.getNext();
      } else {
        // ".apply" call should be filtered before this.
        Preconditions.checkState(!NodeUtil.isFunctionObjectApply(callNode));
      }
    }

    // Before proceeding, ensure that inlining will not change the evaluation
    // order between argument evaluation and side effects in the function body.
    // Specifically, if the function body has any side effects that execute
    // before the first reference to any parameter, then direct inlining could
    // reorder effects relative to argument evaluation and is unsafe.
    {
      // Collect parameter names.
      java.util.Set<String> paramNames = new java.util.HashSet<String>();
      Node params = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
      while (params != null) {
        if (params.isName()) {
          paramNames.add(params.getString());
        }
        params = params.getNext();
      }

      if (!paramNames.isEmpty()) {
        final boolean[] foundRef = new boolean[] {false};
        final boolean[] seBeforeRef = new boolean[] {false};

        // Traverse statements in order until we either find a parameter reference
        // or detect a side effect preceding any parameter reference.
        for (Node stmt = block.getFirstChild();
             stmt != null && !foundRef[0] && !seBeforeRef[0];
             stmt = stmt.getNext()) {
          // Do not descend into function declarations/expressions.
          if (stmt.isFunction()) {
            continue;
          }

          // Evaluation-order aware traversal.
          java.util.Deque<Node> stack = new java.util.ArrayDeque<Node>();
          stack.push(stmt);
          java.util.Deque<Integer> childIndex = new java.util.ArrayDeque<Integer>();
          childIndex.push(Integer.valueOf(0));

          while (!stack.isEmpty() && !foundRef[0] && !seBeforeRef[0]) {
            Node cur = stack.peek();
            if (cur.isFunction()) {
              // Skip inner functions.
              stack.pop();
              childIndex.pop();
              continue;
            }

            // If this node is a NAME referencing a parameter, record and stop.
            if (cur.isName() && paramNames.contains(cur.getString())) {
              foundRef[0] = true;
              break;
            }

            int idx = childIndex.pop().intValue();
            int childCount = cur.getChildCount();

            // Determine evaluation order for special nodes.
            if (idx < childCount) {
              Node nextChild;
              switch (cur.getToken()) {
                case COMMA:
                case HOOK: // condition ? then : else
                case AND:
                case OR:
                case ASSIGN:
                case ASSIGN_ADD:
                case ASSIGN_SUB:
                case ASSIGN_MUL:
                case ASSIGN_DIV:
                case ASSIGN_MOD:
                case ASSIGN_BITOR:
                case ASSIGN_BITXOR:
                case ASSIGN_BITAND:
                case ASSIGN_LSH:
                case ASSIGN_RSH:
                case ASSIGN_URSH:
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case MOD:
                case BITOR:
                case BITXOR:
                case BITAND:
                case LSH:
                case RSH:
                case URSH:
                case LT:
                case LE:
                case GT:
                case GE:
                case EQ:
                case NE:
                case SHEQ:
                case SHNE:
                case INSTANCEOF:
                case IN:
                  // Left-to-right for binary/ternary/comma and assignments
                  nextChild = cur.getChildAtIndex(idx);
                  childIndex.push(Integer.valueOf(idx + 1));
                  stack.push(nextChild);
                  childIndex.push(Integer.valueOf(0));
                  break;
                case CALL:
                case NEW:
                  // callee, then args
                  nextChild = cur.getChildAtIndex(idx);
                  childIndex.push(Integer.valueOf(idx + 1));
                  stack.push(nextChild);
                  childIndex.push(Integer.valueOf(0));
                  break;
                default:
                  // Default left-to-right
                  nextChild = cur.getChildAtIndex(idx);
                  childIndex.push(Integer.valueOf(idx + 1));
                  stack.push(nextChild);
                  childIndex.push(Integer.valueOf(0));
                  break;
              }
            } else {
              // After children have been processed, if no parameter reference was
              // seen yet, check if this node has side effects that would occur now.
              if (!foundRef[0] && NodeUtil.mayHaveSideEffects(cur, compiler)) {
                seBeforeRef[0] = true;
                break;
              }
              stack.pop();
            }
          }
        }

        if (seBeforeRef[0]) {
          return CanInlineResult.NO;
        }
      }
    }

    // FUNCTION NODE -> LP NODE: [ ARG1, ARG2, ... ]
    Node fnParam = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
    while (cArg != null || fnParam != null) {
      // For each named parameter check if a mutable argument use more than one.
      if (fnParam != null) {
        if (cArg != null) {

          // Check for arguments that are evaluated more than once.
          // Note: Unlike block inlining, there it is not possible that a
          // parameter reference will be in a loop.
          if (NodeUtil.mayEffectMutableState(cArg, compiler)
              && NodeUtil.getNameReferenceCount(
                  block, fnParam.getString()) > 1) {
            return CanInlineResult.NO;
          }
        }

        // Move to the next name.
        fnParam = fnParam.getNext();
      }

      // For every call argument check for side-effects, even if there
      // isn't a named parameter to match.
      if (cArg != null) {
        if (NodeUtil.mayHaveSideEffects(cArg, compiler)) {
          return CanInlineResult.NO;
        }
        cArg = cArg.getNext();
      }
    }

    return CanInlineResult.YES;
  }