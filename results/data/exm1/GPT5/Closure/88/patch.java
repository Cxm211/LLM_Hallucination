  private VariableLiveness isVariableReadBeforeKill(
      Node n, String variable) {
    if (NodeUtil.isName(n) && variable.equals(n.getString())) {
      if (NodeUtil.isLhs(n, n.getParent())) {
        // If the variable is on the LHS, determine whether it's an operation
        // that also reads the variable before writing it (e.g. x+=, ++x, x++).
        Node parent = n.getParent();
        if (parent != null) {
          int type = parent.getType();
          // Increment/decrement read the variable before writing.
          if (type == Token.INC || type == Token.DEC) {
            return VariableLiveness.READ;
          }
          // Compound assignments (+=, -=, etc.) read before write.
          if (NodeUtil.isAssignmentOp(parent) && type != Token.ASSIGN) {
            return VariableLiveness.READ;
          }
          // Simple assignment or variable declaration kill the previous value.
          if (type == Token.ASSIGN || type == Token.VAR) {
            return VariableLiveness.KILL;
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