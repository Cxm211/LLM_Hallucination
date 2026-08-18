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
      // Conditionals
      case Token.OR:
      case Token.AND:
        // With AND/OR the first branch always runs, but the second may not.
        Node left = n.getFirstChild();
        Node right = left.getNext();
        VariableLiveness leftState = isVariableReadBeforeKill(left, variable);
        if (leftState != VariableLiveness.MAYBE_LIVE) {
          return leftState;
        }
        // Left is maybe live, so we cannot be sure about the right branch.
        return VariableLiveness.MAYBE_LIVE;
      case Token.HOOK:
        return checkHookBranchReadBeforeKill(
            n.getFirstChild().getNext(), n.getLastChild(), variable);

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