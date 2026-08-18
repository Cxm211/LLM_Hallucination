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

    // Collect parameter names
    java.util.Set<String> paramNames = new java.util.HashSet<>();
    Node fnParamStart = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
    for (Node p = fnParamStart; p != null; p = p.getNext()) {
      paramNames.add(p.getString());
    }

    // Check for dangerous side effects: if the function body may have side effects,
    // we cannot inline if any argument is a non‑parameter variable or a property
    // of a non‑parameter variable, because the side effect could change its value.
    if (NodeUtil.mayHaveSideEffects(block, compiler)) {
      Node arg = cArg;
      while (arg != null) {
        if (arg.isName() && !paramNames.contains(arg.getString())) {
          return CanInlineResult.NO;
        } else if (arg.isGetProp() || arg.isGetElem()) {
          Node base = arg.getFirstChild();
          if (base.isName() && !paramNames.contains(base.getString())) {
            return CanInlineResult.NO;
          }
        }
        arg = arg.getNext();
      }
    }

    // FUNCTION NODE -> LP NODE: [ ARG1, ARG2, ... ]
    Node fnParam = fnParamStart;
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