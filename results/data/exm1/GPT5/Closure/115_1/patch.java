  private CanInlineResult canInlineReferenceDirectly(
      Node callNode, Node fnNode) {
    if (!isDirectCallNodeReplacementPossible(fnNode)) {
      return CanInlineResult.NO;
    }

    Node block = fnNode.getLastChild();

    // Determine if the return expression modifies any of the function parameters
    // (e.g., x = 1, x *= 2, ++x, x++, etc.). If so, we cannot directly inline
    // because we'd need a local alias for the parameter.
    java.util.Set<String> paramNames = new java.util.HashSet<String>();
    {
      Node p = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
      while (p != null) {
        if (p.isName()) {
          paramNames.add(p.getString());
        }
        p = p.getNext();
      }
    }

    boolean modifiesParams = false;
    if (block.hasChildren()) {
      Preconditions.checkState(block.hasOneChild());
      Node stmt = block.getFirstChild();
      if (stmt.isReturn() && stmt.hasChildren()) {
        Node ret = stmt.getFirstChild();
        java.util.ArrayDeque<Node> stack = new java.util.ArrayDeque<Node>();
        stack.push(ret);
        while (!stack.isEmpty() && !modifiesParams) {
          Node n = stack.pop();
          // Assignment operations to a parameter name (e.g., x=..., x+=..., etc.)
          if (NodeUtil.isAssignmentOp(n)) {
            Node lhs = n.getFirstChild();
            if (lhs != null && lhs.isName() && paramNames.contains(lhs.getString())) {
              modifiesParams = true;
              break;
            }
          }
          // ++param or --param (prefix or postfix)
          if (n.isInc() || n.isDec()) {
            Node t = n.getFirstChild();
            if (t != null && t.isName() && paramNames.contains(t.getString())) {
              modifiesParams = true;
              break;
            }
          }
          for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
            stack.push(c);
          }
        }
      }
    }

    // If the function modifies its parameters in the return expression,
    // direct replacement is not safe.
    if (modifiesParams) {
      return CanInlineResult.NO;
    }

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

    // FUNCTION NODE -> LP NODE: [ ARG1, ARG2, ... ]
    Node fnParam = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
    while (cArg != null || fnParam != null) {
      boolean cArgMatchesParam = (fnParam != null);

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

      // For extra call arguments (beyond named parameters) check for side-effects.
      if (cArg != null) {
        if (!cArgMatchesParam && NodeUtil.mayHaveSideEffects(cArg, compiler)) {
          return CanInlineResult.NO;
        }
        cArg = cArg.getNext();
      }
    }

    return CanInlineResult.YES;
  }