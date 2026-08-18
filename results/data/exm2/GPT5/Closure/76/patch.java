  private boolean isVariableStillLiveWithinExpression(
      Node n, Node exprRoot, String variable) {
    while (n != exprRoot) {
      VariableLiveness state = VariableLiveness.MAYBE_LIVE;
      Node parent = n.getParent();
      switch (parent.getType()) {
        case Token.OR:
        case Token.AND:
          if (n == parent.getFirstChild()) {
            Node sibling = n.getNext();
            if (sibling != null && !ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
              state = isVariableReadBeforeKill(sibling, variable);
              if (state == VariableLiveness.READ) {
                return true;
              }
            }
            break;
          }
          // Otherwise, fall through to default behavior.
        case Token.HOOK:
          if (n == parent.getFirstChild()) {
            Node trueNode = parent.getFirstChild().getNext();
            Node falseNode = parent.getLastChild();
            if (!ControlFlowGraph.isEnteringNewCfgNode(trueNode)) {
              state = isVariableReadBeforeKill(trueNode, variable);
              if (state == VariableLiveness.READ) {
                return true;
              }
            }
            if (!ControlFlowGraph.isEnteringNewCfgNode(falseNode)) {
              state = isVariableReadBeforeKill(falseNode, variable);
              if (state == VariableLiveness.READ) {
                return true;
              }
            }
            break;
          } else {
            break;
          }
        default:
          for (Node sibling = n.getNext(); sibling != null; sibling = sibling.getNext()) {
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