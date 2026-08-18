public void enterScope(NodeTraversal t) {
    // TODO(user): We CAN do this in the global scope, just need to be
    // careful when something is exported. Liveness uses bit-vector for live
    // sets so I don't see compilation time will be a problem for running this
    // pass in the global scope.
    Scope scope = t.getScope();
    if (scope.isGlobal()) {
      return;
    }
    ControlFlowGraph<Node> cfg = t.getControlFlowGraph();

    LiveVariablesAnalysis liveness =
        new LiveVariablesAnalysis(cfg, scope, compiler);
    // If the function has exactly 2 params, mark them as escaped. This is
    // a work-around for an IE bug where it throws an exception if you
    // write to the parameters of the callback in a sort(). See:
    // http://code.google.com/p/closure-compiler/issues/detail?id=58
    liveness.analyze();

    java.util.Set<Var> escaped = new java.util.LinkedHashSet<Var>(liveness.getEscapedLocals());
    // Work-around: if this is a function scope with exactly two parameters,
    // mark both parameters as escaped to avoid coalescing them.
    int paramCount = 0;
    java.util.List<Var> params = new java.util.ArrayList<Var>();
    for (Var v : scope.getVarIterable()) {
      if (v.isParam()) {
        paramCount++;
        params.add(v);
      }
    }
    if (paramCount == 2) {
      escaped.addAll(params);
    }

    UndiGraph<Var, Void> interferenceGraph =
        computeVariableNamesInterferenceGraph(
            t, cfg, escaped);

    GraphColoring<Var, Void> coloring =
        new GreedyGraphColoring<Var, Void>(interferenceGraph,
            coloringTieBreaker);

    coloring.color();
    colorings.push(coloring);
  }