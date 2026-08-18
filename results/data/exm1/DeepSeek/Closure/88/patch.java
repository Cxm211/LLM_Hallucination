  private VariableLiveness isVariableReadBeforeKill(
      Node n, String variable) {
    if (NodeUtil.isName(n) && variable.equals(n.getString())) {
      if (NodeUtil.isLhs(n, n.getParent())) {
        // The expression to which the assignment is made is evaluated before
        // the RHS is evaluated (normal left to right evaluation) but the KILL
        // occurs after the RHS is evaluated.
        // We defer the kill to handle reads in the RHS.
      } else {
        return VariableLiveness.READ;
      }
    }

    // For assignment nodes, process RHS first (since kill occurs after RHS).
    boolean isAssign = n.isAssign();
    Node lhs = null;
    for (Node child = n.getFirstChild();
        child != null; child = child.getNext()) {
      if (isAssign && child == n.getFirstChild()) {
        lhs = child;
        continue; // Skip LHS for now; process it after RHS.
      }
      if (!ControlFlowGraph.isEnteringNewCfgNode(child)) {
        VariableLiveness state = isVariableReadBeforeKill(child, variable);
        if (state != VariableLiveness.MAYBE_LIVE) {
          return state;
        }
      }
    }

    // After processing RHS, handle the LHS kill for assignments.
    if (isAssign && lhs != null) {
      VariableLiveness state = isVariableReadBeforeKill(lhs, variable);
      if (state == VariableLiveness.KILL || state == VariableLiveness.MAYBE_LIVE) {
        return VariableLiveness.KILL;
      }
      if (state != VariableLiveness.MAYBE_LIVE) {
        return state;
      }
    }

    return VariableLiveness.MAYBE_LIVE;
  }