  private boolean isVariableStillLiveWithinExpression(
      Node n, Node exprRoot, String variable) {
    while (n != exprRoot) {
      VariableLiveness state = VariableLiveness.MAYBE_LIVE;
      Node parent = n.getParent();
      switch (parent.getType()) {
        case Token.OR:
        case Token.AND:
          // If the current node is the first child of AND/OR,
          // only consider the second operand.
          if (n == parent.getFirstChild()) {
            Node secondChild = parent.getFirstChild().getNext();
            state = isVariableReadBeforeKill(secondChild, variable);
            if (state == VariableLiveness.READ) {
              return true;
            } else if (state == VariableLiveness.KILL) {
              return false;
            }
          }
          // If n is the second child, no siblings to check.
          break;
        case Token.HOOK:
          {
            Node condition = parent.getFirstChild();
            Node thenBranch = condition.getNext();
            Node elseBranch = thenBranch.getNext();
            if (n == condition) {
              // In condition: check both branches.
              state = isVariableReadBeforeKill(thenBranch, variable);
              if (state == VariableLiveness.READ) {
                return true;
              } else if (state == VariableLiveness.KILL) {
                return false;
              }
              state = isVariableReadBeforeKill(elseBranch, variable);
              if (state == VariableLiveness.READ) {
                return true;
              } else if (state == VariableLiveness.KILL) {
                return false;
              }
            } else if (n == thenBranch || n == elseBranch) {
              // In a branch: the other branch does not execute, so skip.
              // No need to check siblings.
            }
          }
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
      }
      n = parent;
    }
    return false;
  }