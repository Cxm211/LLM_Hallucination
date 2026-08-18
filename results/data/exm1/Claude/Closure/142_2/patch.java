public void enterScope(NodeTraversal t) {
  Scope scope = t.getScope();
  if (scope.isGlobal()) {
    return;
  }
  ControlFlowGraph<Node> cfg = t.getControlFlowGraph();

  LiveVariablesAnalysis liveness =
      new LiveVariablesAnalysis(cfg, scope, compiler);
  liveness.analyze();
  if (scope.getRootNode().getType() == Token.FUNCTION) {
    Node paramList = scope.getRootNode().getFirstChild().getNext();
    if (paramList != null && paramList.getChildCount() == 2) {
      liveness.markAllParametersEscaped();
    }
  }

  UndiGraph<Var, Void> interferenceGraph =
      computeVariableNamesInterferenceGraph(
          t, cfg, liveness.getEscapedLocals());

  GraphColoring<Var, Void> coloring =
      new GreedyGraphColoring<Var, Void>(interferenceGraph,
          coloringTieBreaker);

  coloring.color();
  colorings.push(coloring);
}