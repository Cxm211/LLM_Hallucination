public void enterScope(NodeTraversal t) {
    Scope scope = t.getScope();
    if (scope.isGlobal()) {
      return;
    }
    ControlFlowGraph<Node> cfg = t.getControlFlowGraph();

    LiveVariablesAnalysis liveness =
        new LiveVariablesAnalysis(cfg, scope, compiler);

    // If the function has exactly 2 params, mark them as escaped.
    // This is a work-around for an IE bug where it throws an exception if you
    // write to the parameters of the callback in a sort(). See:
    // http://code.google.com/p/closure-compiler/issues/detail?id=58
    Node fnNode = t.getScope().getRootNode();
    if (fnNode.getParamCount() == 2) {
      for (Node param = fnNode.getFirstChild(); param != null; param = param.getNext()) {
        if (param.isParamList()) {
          for (Node p = param.getFirstChild(); p != null; p = p.getNext()) {
            Var var = t.getScope().getVar(p.getString());
            if (var != null) {
              liveness.getEscapedLocals().add(var);
            }
          }
          break;
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