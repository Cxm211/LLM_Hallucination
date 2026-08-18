public void enterScope(NodeTraversal t) {
  Scope scope = t.getScope();
  if (scope.isGlobal()) {
    return;
  }
  ControlFlowGraph<Node> cfg = t.getControlFlowGraph();

  LiveVariablesAnalysis liveness =
      new LiveVariablesAnalysis(cfg, scope, compiler);
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