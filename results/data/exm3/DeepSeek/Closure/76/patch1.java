  private VariableLiveness isVariableReadBeforeKill(
      Node n, String variable) {

    if (NodeUtil.isName(n) && variable.equals(n.getString())) {
      if (NodeUtil.isLhs(n, n.getParent())) {
        Preconditions.checkState(n.getParent().getType() == Token.ASSIGN);
        // The expression to which the assignment is made is evaluated before
        // the RHS is evaluated (normal left to right evaluation) but the KILL
        // occurs after the RHS is evaluated.
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
        {
          // Check left child first.
          VariableLiveness leftState =
              isVariableReadBeforeKill(n.getFirstChild(), variable);
          if (leftState != VariableLiveness.MAYBE_LIVE) {
            return leftState;
          }
          // Check right child conditionally.
          VariableLiveness rightState =
              isVariableReadBeforeKill(n.getLastChild(), variable);
          // If there is a read in the right child, it is conditional, so we
          // return READ to be conservative.
          if (rightState == VariableLiveness.READ) {
            return VariableLiveness.READ;
          } else {
            // For KILL or MAYBE_LIVE, we cannot be certain because the right
            // child may not execute.
            return VariableLiveness.MAYBE_LIVE;
          }
        }
      case Token.HOOK:
        {
          // Check the condition first.
          Node condition = n.getFirstChild();
          VariableLiveness condState = isVariableReadBeforeKill(condition, variable);
          if (condState != VariableLiveness.MAYBE_LIVE) {
            return condState;
          }
          // Then check the two branches conditionally.
          return checkHookBranchReadBeforeKill(
              condition.getNext(), condition.getNext().getNext(), variable);
        }
      default:
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
    }

    return VariableLiveness.MAYBE_LIVE;
  }