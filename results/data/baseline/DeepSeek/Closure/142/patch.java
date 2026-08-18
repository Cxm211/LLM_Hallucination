public void enterScope(NodeTraversal t) {
    Scope scope = t.getScope();
    if (scope.isGlobal()) {
      return;
    }
    ControlFlowGraph<Node> cfg = t.getControlFlowGraph();

    LiveVariablesAnalysis liveness =
        new LiveVariablesAnalysis(cfg, scope, compiler);

    // Workaround for IE bug: if the function has exactly 2 params, mark them as escaped.
    Node functionNode = scope.getRootNode();
    Node paramList = NodeUtil.getFunctionParameters(functionNode);
    if (paramList != null && paramList.getChildCount() == 2) {
      for (Node param = paramList.getFirstChild(); param != null; param = param.getNext()) {
        String paramName = param.getString();
        Var var = scope.getVar(paramName);
        if (var != null) {
          liveness.addEscapedLocal(var);
        }
      }
    }

    liveness.analyze();

    UndiGraph<Var, Void> interferenceGraph =
        computeVariableNamesInterferenceGraph(
            t, cfg, liveness.getEscapedLocals());

    GraphColoring<Var, Void> coloring =
        new GreedyGraphColoring<Var, Void>(interferenceGraph,
            coloringTieBreaker);

    coloring.color();
    colorings.push(coloring);
  }