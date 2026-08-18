private boolean isVariableStillLiveWithinExpression(Node n, Node exprRoot, String variable) {
  while (n != exprRoot) {
    VariableLiveness state = VariableLiveness.MAYBE_LIVE;
    switch (n.getParent().getType()) {
      case Token.OR:
      case Token.AND:
        if (n == n.getParent().getFirstChild()) {
          state = isVariableReadBeforeKill(n.getNext(), variable);
          if (state == VariableLiveness.READ) {
            return true;
          } else if (state == VariableLiveness.KILL) {
            return false;
          }
        }
        break;
      case Token.HOOK:
        if (n == n.getParent().getFirstChild()) {
          state = checkHookBranchReadBeforeKill(n.getNext(), n.getNext().getNext(), variable);
          if (state == VariableLiveness.READ) {
            return true;
          } else if (state == VariableLiveness.KILL) {
            return false;
          }
        }
        break;
      default:
        for(Node sibling = n.getNext(); sibling != null; sibling = sibling.getNext()) {
          if (!ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
            state = isVariableReadBeforeKill(sibling, variable);
            if (state == VariableLiveness.READ) {
              return true;
            } else if (state == VariableLiveness.KILL) {
              return false;
            }
          }
        }
    }
    n = n.getParent();
  }
  return false;
}