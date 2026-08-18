private boolean isVariableStillLiveWithinExpression(Node n, Node exprRoot, String variable) {
  while (n != exprRoot) {
    VariableLiveness state = VariableLiveness.MAYBE_LIVE;
    Node parent = n.getParent();
    switch (parent.getType()) {
      case Token.OR:
      case Token.AND: {
        // If the current node is the first child of AND/OR, only consider the
        // reads/kills in the second operand. If it's the second child, there
        // are no following siblings in this parent to consider.
        if (parent.getFirstChild() == n) {
          Node sibling = n.getNext();
          if (sibling != null && !ControlFlowGraph.isEnteringNewCfgNode(sibling)) {
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
      case Token.HOOK: {
        // If current node is the condition, check both branches. Otherwise,
        // it's within one branch and the other can be ignored.
        Node cond = parent.getFirstChild();
        Node thenBranch = cond.getNext();
        Node elseBranch = thenBranch.getNext();
        if (n == cond) {
          if (!ControlFlowGraph.isEnteringNewCfgNode(thenBranch)) {
            state = isVariableReadBeforeKill(thenBranch, variable);
            if (state == VariableLiveness.READ) {
              return true;
            } else if (state == VariableLiveness.KILL) {
              return false;
            }
          }
          if (!ControlFlowGraph.isEnteringNewCfgNode(elseBranch)) {
            state = isVariableReadBeforeKill(elseBranch, variable);
            if (state == VariableLiveness.READ) {
              return true;
            } else if (state == VariableLiveness.KILL) {
              return false;
            }
          }
        }
        break;
      }
      default: {
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
      }
    }
    n = parent;
  }
  return false;
}
