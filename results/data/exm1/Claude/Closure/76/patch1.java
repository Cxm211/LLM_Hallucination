private VariableLiveness isVariableReadBeforeKill(Node n, String variable) {
  if (NodeUtil.isName(n) && variable.equals(n.getString())) {
    if (NodeUtil.isLhs(n, n.getParent())) {
      Preconditions.checkState(n.getParent().getType() == Token.ASSIGN);
      Node rhs = n.getNext();
      VariableLiveness state = isVariableReadBeforeKill(rhs, variable);
      if (state == VariableLiveness.READ) {
        return state;
      }
      return VariableLiveness.KILL;
    } else {
      return VariableLiveness.READ;
    }
  }
  switch (n.getType()) {
    case Token.OR:
    case Token.AND:
      VariableLiveness first = isVariableReadBeforeKill(n.getFirstChild(), variable);
      if (first != VariableLiveness.MAYBE_LIVE) {
        return first;
      }
      return checkHookBranchReadBeforeKill(n.getFirstChild().getNext(), n.getLastChild(), variable);
    case Token.HOOK:
      return checkHookBranchReadBeforeKill(n.getFirstChild().getNext(), n.getLastChild(), variable);
    default:
      for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
        if (!ControlFlowGraph.isEnteringNewCfgNode(child)) {
          VariableLiveness state = isVariableReadBeforeKill(child, variable);
          if (state != VariableLiveness.MAYBE_LIVE) {
            return state;
          }
        }
      }
  }
  return VariableLiveness.MAYBE_LIVE;
}