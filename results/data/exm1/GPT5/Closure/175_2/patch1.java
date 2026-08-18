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

    Node fnParam = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
    while (cArg != null || fnParam != null) {
      if (fnParam != null) {
        if (cArg != null) {
          int refCount = NodeUtil.getNameReferenceCount(block, fnParam.getString());
          if (NodeUtil.mayEffectMutableState(cArg, compiler)
              && refCount > 1) {
            return CanInlineResult.NO;
          }
          // If the argument may have side effects but the parameter is never
          // referenced, inlining would drop the side effects.
          if (NodeUtil.mayHaveSideEffects(cArg, compiler)
              && refCount == 0) {
            return CanInlineResult.NO;
          }
        }
        fnParam = fnParam.getNext();
      } else {
        // More arguments than parameters; extra arguments must not have side effects
        if (cArg != null && NodeUtil.mayHaveSideEffects(cArg, compiler)) {
          return CanInlineResult.NO;
        }
      }
      if (cArg != null) {
        cArg = cArg.getNext();
      }
    }

    return CanInlineResult.YES;
  }