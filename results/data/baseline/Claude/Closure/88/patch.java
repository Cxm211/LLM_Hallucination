private VariableLiveness isVariableReadBeforeKill(
      Node n, String variable) {
    if (NodeUtil.isName(n) && variable.equals(n.getString())) {
      if (NodeUtil.isLhs(n, n.getParent())) {
        Node parent = n.getParent();
        Node rhs = null;
        if (parent != null && parent.getChildCount() >= 2) {
          rhs = parent.getLastChild();
        }
        if (rhs != null) {
          VariableLiveness rhsState = isVariableReadBeforeKill(rhs, variable);
          if (rhsState == VariableLiveness.READ) {
            return VariableLiveness.READ;
          }
        }
        return VariableLiveness.KILL;
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