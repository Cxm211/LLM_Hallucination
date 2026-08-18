  private VariableLiveness isVariableReadBeforeKill(
      Node n, String variable) {
    if (NodeUtil.isName(n) && variable.equals(n.getString())) {
      if (NodeUtil.isLhs(n, n.getParent())) {
        // The expression to which the assignment is made is evaluated before
        // the RHS is evaluated (normal left to right evaluation) but the KILL
        // occurs after the RHS is evaluated.
        return VariableLiveness.KILL;
      } else {
        return VariableLiveness.READ;
      }
    }

    // For assignment nodes, the right-hand side is evaluated before the left-hand side.
    if (n.isAssign()) {
      // Check the RHS first.
      Node rhs = n.getLastChild();
      VariableLiveness rhsState = isVariableReadBeforeKill(rhs, variable);
      if (rhsState != VariableLiveness.MAYBE_LIVE) {
        return rhsState;
      }
      // Then check the LHS.
      Node lhs = n.getFirstChild();
      return isVariableReadBeforeKill(lhs, variable);
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