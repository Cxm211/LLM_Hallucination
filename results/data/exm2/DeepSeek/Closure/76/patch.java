  private boolean isVariableStillLiveWithinExpression(
      Node n, Node exprRoot, String variable) {
    while (n != exprRoot) {
      VariableLiveness state = VariableLiveness.MAYBE_LIVE;
      switch (n.getParent().getType()) {
        case Token.OR:
        case Token.AND:
          // If the current node is the first child of AND/OR,
          // only consider the second operand.
          if (n == n.getParent().getFirstChild()) {
            Node second = n.getNext();
            if (second != null) {
              state = isVariableReadBeforeKill(second, variable);
              if (state == VariableLiveness.READ) {
                return true;
              } else if (state == VariableLiveness.KILL) {
                return false;
              }
            }
          }
          // If n is the second child, there are no siblings to consider.
          break;
        case Token.HOOK:
          // If current node is the condition, check both branches.
          // Otherwise, ignore the other branch.
          Node condition = n.getParent().getFirstChild();
          if (n == condition) {
            Node trueBranch = condition.getNext();
            Node falseBranch = n.getParent().getLastChild();
            VariableLiveness trueState = isVariableReadBeforeKill(trueBranch, variable);
            if (trueState == VariableLiveness.READ) {
              return true;
            } else if (trueState == VariableLiveness.KILL) {
              return false;
            }
            VariableLiveness falseState = isVariableReadBeforeKill(falseBranch, variable);
            if (falseState == VariableLiveness.READ) {
              return true;
            } else if (falseState == VariableLiveness.KILL) {
              return false;
            }
          }
          break;
        default:
          for (Node sibling = n.getNext(); sibling != null;
               sibling = sibling.getNext()) {
            if (!ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
              state = isVariableReadBeforeKill(sibling, variable);

              // If we see a READ or KILL there is no need to continue.
              if (state == VariableLiveness.READ) {
                return true;
              } else if (state == VariableLiveness.KILL) {
                return false;
              }
            }
          }
      }
      n = n.getParent();
    }
    return false;
  }