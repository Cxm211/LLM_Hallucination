  private VariableLiveness isVariableReadBeforeKill(
      Node n, String variable) {
    if (NodeUtil.isName(n) && variable.equals(n.getString())) {
      Node p = n.getParent();
      if (NodeUtil.isLhs(n, p)) {
        if (p != null) {
          if (p.isAssign()) {
            Node rhs = n.getNext();
            if (rhs != null) {
              VariableLiveness state = isVariableReadBeforeKill(rhs, variable);
              if (state == VariableLiveness.READ) {
                return VariableLiveness.READ;
              }
            }
            return VariableLiveness.KILL;
          } else if (NodeUtil.isAssignmentOp(p) || p.isInc() || p.isDec()) {
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
