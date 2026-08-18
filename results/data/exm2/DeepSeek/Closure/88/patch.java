private VariableLiveness isVariableReadBeforeKill(
      Node n, String variable) {
    if (NodeUtil.isName(n) && variable.equals(n.getString())) {
      if (NodeUtil.isLhs(n, n.getParent())) {
        Node parent = n.getParent();
        if (NodeUtil.isAssignmentOp(parent) && parent.getType() == Token.ASSIGN) {
          // Simple assignment: check RHS for reads before considering this kill.
          Node rhs = parent.getSecondChild();
          VariableLiveness rhsState = isVariableReadBeforeKill(rhs, variable);
          if (rhsState == VariableLiveness.READ) {
            return VariableLiveness.READ;
          }
          return VariableLiveness.KILL;
        } else if (NodeUtil.isAssignmentOp(parent)) {
          // Compound assignment: left operand is read.
          return VariableLiveness.READ;
        } else {
          return VariableLiveness.KILL;
        }
      } else {
        return VariableLiveness.READ;
      }
    }

    // Expressions are evaluated left-right, depth first.
    for (Node child = n.getFirstChild();
        child != null; child = child.getNext()) {
      if (!ControlFlowGraph.isEnteringNewCfgNode(child)) { // Not a FUNCTION
        VariableLiveness state = isVariableReadBeforeKill(child, variable);
        if (state != VariableLiveness.MAYBE_LIVE) {
          return state;
        }
      }
    }
    return VariableLiveness.MAYBE_LIVE;
  }