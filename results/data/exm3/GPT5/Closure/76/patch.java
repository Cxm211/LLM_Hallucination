private boolean isVariableStillLiveWithinExpression(
      Node n, Node exprRoot, String variable) {
    while (n != exprRoot) {
      VariableLiveness state = VariableLiveness.MAYBE_LIVE;
      Node parent = n.getParent();
      switch (parent.getType()) {
        case Token.OR:
        case Token.AND:
          // If the current node is the first child of AND/OR, be conservative and
          // only consider READs of the second operand (since it may not execute).
          if (n == parent.getFirstChild()) {
            Node second = n.getNext();
            if (second != null && !ControlFlowGraph.isEnteringNewCfgNode(second)) {
              state = isVariableReadBeforeKill(second, variable);
              if (state == VariableLiveness.READ) {
                return true;
              }
              // Ignore KILL in second operand since it may not execute.
            }
          }
          // If n is the second child, nothing inside parent remains to evaluate.
          break;

        case Token.HOOK:
          // If current node is the condition, check each following branch; otherwise,
          // it is a conditional branch and the other branch can be ignored.
          if (n == parent.getFirstChild()) {
            Node thenBranch = n.getNext();
            Node elseBranch = (thenBranch != null) ? thenBranch.getNext() : null;
            if (thenBranch != null && !ControlFlowGraph.isEnteringNewCfgNode(thenBranch)) {
              state = isVariableReadBeforeKill(thenBranch, variable);
              if (state == VariableLiveness.READ) {
                return true;
              }
            }
            if (elseBranch != null && !ControlFlowGraph.isEnteringNewCfgNode(elseBranch)) {
              state = isVariableReadBeforeKill(elseBranch, variable);
              if (state == VariableLiveness.READ) {
                return true;
              }
            }
          }
          // If n is a branch, no need to check the other branch here.
          break;

        default:
          for (Node sibling = n.getNext(); sibling != null; sibling = sibling.getNext()) {
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
          break;
      }
      n = parent;
    }
    return false;
  }