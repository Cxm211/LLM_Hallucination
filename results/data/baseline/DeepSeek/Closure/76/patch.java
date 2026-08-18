  private boolean isVariableStillLiveWithinExpression(
      Node n, Node exprRoot, String variable) {
    while (n != exprRoot) {
      VariableLiveness state = VariableLiveness.MAYBE_LIVE;
      switch (n.getParent().getType()) {
        case Token.OR:
        case Token.AND:
          // If the current node is the first child of AND/OR,
          // only consider the READs of the second operand.
          if (n == n.getParent().getFirstChild()) {
            Node secondChild = n.getNext();
            if (secondChild != null &&
                !ControlFlowGraph.isEnteringNewCfgNode(secondChild)) {
              state = isVariableReadBeforeKill(secondChild, variable);
              if (state == VariableLiveness.READ) {
                return true;
              } else if (state == VariableLiveness.KILL) {
                return false;
              }
            }
          }
          break;
        case Token.HOOK:
          // If current node is the condition, check each following branch.
          if (n == n.getParent().getFirstChild()) {
            Node trueBranch = n.getNext();
            Node falseBranch = n.getParent().getLastChild();
            if (trueBranch != null &&
                !ControlFlowGraph.isEnteringNewCfgNode(trueBranch)) {
              state = isVariableReadBeforeKill(trueBranch, variable);
              if (state == VariableLiveness.READ) {
                return true;
              } else if (state == VariableLiveness.KILL) {
                return false;
              }
            }
            if (falseBranch != null &&
                !ControlFlowGraph.isEnteringNewCfgNode(falseBranch)) {
              state = isVariableReadBeforeKill(falseBranch, variable);
              if (state == VariableLiveness.READ) {
                return true;
              } else if (state == VariableLiveness.KILL) {
                return false;
              }
            }
          }
          // If n is inside a branch, ignore the other branch.
          break;
        default:
          for (Node sibling = n.getNext(); sibling != null;
               sibling = sibling.getNext()) {
            if (!ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
              state = isVariableReadBeforeKill(sibling, variable);
              if (state == VariableLiveness.READ) {
                return true;
              } else if (state == VariableLiveness.KILL) {
                return false;
              }
            }
          }
          break;
      }
      n = n.getParent();
    }
    return false;
  }
