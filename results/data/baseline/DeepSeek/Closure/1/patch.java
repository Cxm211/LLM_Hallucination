private void removeUnreferencedFunctionArgs(Scope fnScope) {
    Node function = fnScope.getRootNode();
    Preconditions.checkState(function.isFunction());
    if (NodeUtil.isGetOrSetKey(function.getParent())) {
      return;
    }
    Node argList = getFunctionArgList(function);
    boolean modifyCallers = modifyCallSites
        && callSiteOptimizer.canModifyCallers(function);
    if (!modifyCallers) {
      Node lastArg;
      while ((lastArg = argList.getLastChild()) != null) {
        String name = null;
        if (lastArg.isName()) {
          name = lastArg.getString();
        } else if (lastArg.isDefault()) {
          name = lastArg.getFirstChild().getString();
        } else if (lastArg.isRest()) {
          name = lastArg.getString();
        } else {
          break;
        }
        if (name == null) break;
        Var var = fnScope.getVar(name);
        if (var != null && !referenced.contains(var)) {
          argList.removeChild(lastArg);
          compiler.reportCodeChange();
        } else {
          break;
        }
      }
    } else {
      callSiteOptimizer.optimize(fnScope, referenced);
    }
  }