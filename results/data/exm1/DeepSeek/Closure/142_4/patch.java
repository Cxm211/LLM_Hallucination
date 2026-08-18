public void enterScope(NodeTraversal t) {
    Scope scope = t.getScope();
    if (scope.isGlobal()) {
      return;
    }
    ControlFlowGraph<Node> cfg = t.getControlFlowGraph();

    LiveVariablesAnalysis liveness =
        new LiveVariablesAnalysis(cfg, scope, compiler);
    liveness.analyze();

    // Workaround for IE bug: if function has exactly 2 params,
    // mark them as escaped to prevent issues in sort callback.
    Node functionNode = scope.getRootNode();
    if (functionNode.getType() == Token.FUNCTION) {
      int paramCount = functionNode.getChildCount() - 2; // subtract name and body
      if (paramCount == 2) {
        Set<Var> escapedLocals = liveness.getEscapedLocals();
        Node params = functionNode.getFirstChild().getNext(); // params node
        for (Node param = params.getFirstChild(); param != null; param = param.getNext()) {
          Var var = scope.getVar(param.getString());
          if (var != null) {
            escapedLocals.add(var);
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