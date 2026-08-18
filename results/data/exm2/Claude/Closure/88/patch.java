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
    
    // Special handling for logical AND operator (&&):
    // In expressions like (x=1) && (x=2), the second assignment should be treated
    // as a KILL without a READ, because the left-hand side assignment defines x
    // before the right-hand side is evaluated.
    if (n.getToken() == Token.AND || n.getToken() == Token.OR) {
      Node left = n.getFirstChild();
      Node right = left.getNext();
      VariableLiveness leftState = isVariableReadBeforeKill(left, variable);
      if (leftState == VariableLiveness.KILL) {
        VariableLiveness rightState = isVariableReadBeforeKill(right, variable);
        if (rightState == VariableLiveness.KILL) {
          return VariableLiveness.KILL;
        }
      }
    }
    return VariableLiveness.MAYBE_LIVE;
  }