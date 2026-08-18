CanInlineResult canInlineReferenceToFunction(NodeTraversal t,
      Node callNode, Node fnNode, Set<String> needAliases,
      InliningMode mode, boolean referencesThis, boolean containsFunctions) {
    if (!isSupportedCallType(callNode)) {
      return CanInlineResult.NO;
    }

    if (containsFunctions) {
      if (!assumeMinimumCapture && !t.inGlobalScope()) {
        return CanInlineResult.NO;
      } else if (NodeUtil.isWithinLoop(callNode)) {
        return CanInlineResult.NO;
      }
    }

    if (referencesThis && !NodeUtil.isFunctionObjectCall(callNode)) {
      return CanInlineResult.NO;
    }

    if (mode == InliningMode.DIRECT) {
      return canInlineReferenceDirectly(callNode, fnNode);
    } else {
      return canInlineReferenceAsStatementBlock(
          t, callNode, fnNode, needAliases);
    }
  }