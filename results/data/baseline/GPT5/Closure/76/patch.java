private boolean isVariableStillLiveWithinExpression(
      Node n, Node exprRoot, String variable) {
    while (n != exprRoot) {
      VariableLiveness state = VariableLiveness.MAYBE_LIVE;
      for (Node sibling = n.getNext(); sibling != null; sibling = sibling.getNext()) {
        if (!ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
          state = isVariableReadBeforeKill(sibling, variable);
          if (state == VariableLiveness.READ) {
            return true;
          } else if (state == VariableLiveness.KILL) {
            return false;
          }
        }
      }
      n = n.getParent();
    }
    return false;
  }