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

    // Start with the escaped locals discovered by liveness.
    Set<Var> escaped = new HashSet<Var>(liveness.getEscapedLocals());
    // Work-around: if the function has exactly 2 parameters, mark them escaped.
    Node root = scope.getRootNode();
    if (root != null && root.getFirstChild() != null) {
      // In a FUNCTION node, params are the second child.
      Node params = root.getSecondChild();
      if (params != null) {
        int paramCount = 0;
        for (Node c = params.getFirstChild(); c != null; c = c.getNext()) {
          paramCount++;
        }
        if (paramCount == 2) {
          for (Node c = params.getFirstChild(); c != null; c = c.getNext()) {
            String name = c.getString();
            Var v = scope.getVar(name);
            if (v != null) {
              escaped.add(v);
            }
          }
        }
      }
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