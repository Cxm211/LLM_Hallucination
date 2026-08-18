private void removeUnreferencedFunctionArgs(Scope fnScope) {
    // Notice that removing unreferenced function args breaks
    // Function.prototype.length. In advanced mode, we don't really care
    // about this: we consider "length" the equivalent of reflecting on
    // the function's lexical source.
    //
    // Rather than create a new option for this, we assume that if the user
    // is removing globals, then it's OK to remove unused function args.
    //
    // See http://code.google.com/p/closure-compiler/issues/detail?id=253

    Node function = fnScope.getRootNode();

    Preconditions.checkState(function.isFunction());
    if (NodeUtil.isGetOrSetKey(function.getParent())) {
      // The parameters object literal setters can not be removed.
      return;
    }

    Node argList = getFunctionArgList(function);
    boolean modifyCallers = modifyCallSites
        && callSiteOptimizer.canModifyCallers(function);
    if (!modifyCallers) {
      // Remove all unreferenced args, not just trailing ones.
      Node arg = argList.getFirstChild();
      while (arg != null) {
        Node next = arg.getNextSibling();
        if (arg.isName()) {
          Var var = fnScope.getVar(arg.getString());
          if (var != null && !referenced.contains(var)) {
            argList.removeChild(arg);
            compiler.reportCodeChange();
          }
        }
        arg = next;
      }
    } else {
      callSiteOptimizer.optimize(fnScope, referenced);
    }
  }