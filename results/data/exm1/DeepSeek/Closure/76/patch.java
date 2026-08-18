  private boolean isVariableStillLiveWithinExpression(
      Node n, Node exprRoot, String variable) {
    while (n != exprRoot) {
      VariableLiveness state = VariableLiveness.MAYBE_LIVE;
      switch (n.getParent().getType()) {
        case Token.OR:
        case Token.AND:
          // If the currently node is the first child of
          // AND/OR, be conservative only consider the READs
          // of the second operand.
        case Token.HOOK:
          // If current node is the condition, check each following
          // branch, otherwise it is a conditional branch and the
          // other branch can be ignored.
          if (n.getParent().getType() == Token.HOOK) {
            if (n == n.getParent().getFirstChild()) {
              // condition: check both branches
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
            // else branch: ignore other branch, do nothing
          } else {
            // AND/OR: treat as default
            for(Node sibling = n.getNext(); sibling != null;
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
          break;
        default:
          for(Node sibling = n.getNext(); sibling != null;
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