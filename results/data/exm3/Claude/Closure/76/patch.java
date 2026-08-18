private boolean isVariableStillLiveWithinExpression(
      Node n, Node exprRoot, String variable) {
    while (n != exprRoot) {
      VariableLiveness state = VariableLiveness.MAYBE_LIVE;
      switch (n.getParent().getType()) {
        case Token.OR:
        case Token.AND:
          // If the current node is the first child of
          // AND/OR, be conservative and only consider the READs
          // of the second operand.
          if (n == n.getParent().getFirstChild()) {
            Node secondOperand = n.getNext();
            if (secondOperand != null && !ControlFlowGraph.isEnteringNewCfgNode(secondOperand)) {
              state = isVariableReadBeforeKill(secondOperand, variable);
            }
          } else {
            // If current node is the second operand, check siblings
            for (Node sibling = n.getNext(); sibling != null; sibling = sibling.getNext()) {
              if (!ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
                state = isVariableReadBeforeKill(sibling, variable);
                if (state != VariableLiveness.MAYBE_LIVE) {
                  break;
                }
              }
            }
          }
          break;

        case Token.HOOK:
          // If current node is the condition, check each following
          // branch, otherwise it is a conditional branch and the
          // other branch can be ignored.
          if (n == n.getParent().getFirstChild()) {
            // Current node is the condition, check both branches
            Node trueBranch = n.getNext();
            Node falseBranch = n.getParent().getLastChild();
            if (trueBranch != null && falseBranch != null) {
              state = checkHookBranchReadBeforeKill(trueBranch, falseBranch, variable);
            }
          } else {
            // Current node is one of the branches, check siblings after the HOOK
            for (Node sibling = n.getNext(); sibling != null; sibling = sibling.getNext()) {
              if (!ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
                state = isVariableReadBeforeKill(sibling, variable);
                if (state != VariableLiveness.MAYBE_LIVE) {
                  break;
                }
              }
            }
          }
          break;

        default:
          for (Node sibling = n.getNext(); sibling != null; sibling = sibling.getNext()) {
            if (!ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
              state = isVariableReadBeforeKill(sibling, variable);
              if (state != VariableLiveness.MAYBE_LIVE) {
                break;
              }
            }
          }
      }

      // If we see a READ or KILL there is no need to continue.
      if (state == VariableLiveness.READ) {
        return true;
      } else if (state == VariableLiveness.KILL) {
        return false;
      }
      n = n.getParent();
    }
    return false;
  }