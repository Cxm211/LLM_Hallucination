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

    // Fix: mark escaped locals for functions with exactly 2 parameters
    Node functionNode = scope.getRootNode();
    if (functionNode.isFunction()) {
      Node params = functionNode.getFirstChild().getNext();
      if (params != null && params.getChildCount() == 2) {
        for (Node param = params.getFirstChild(); param != null; param = param.getNext()) {
          String paramName = param.getString();
          Var var = scope.getVar(paramName);
          if (var != null) {
            liveness.getEscapedLocals().add(var);
          }
        }
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