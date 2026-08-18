private static boolean mayThrowException(Node n) {
    switch (n.getType()) {
      case Token.CALL:
      case Token.GETPROP:
      case Token.GETELEM:
      case Token.THROW:
      case Token.NEW:
      case Token.ASSIGN:
      case Token.INC:
      case Token.DEC:
      case Token.ASSIGN_BITOR:
      case Token.ASSIGN_BITXOR:
      case Token.ASSIGN_BITAND:
      case Token.ASSIGN_LSH:
      case Token.ASSIGN_RSH:
      case Token.ASSIGN_URSH:
      case Token.ASSIGN_ADD:
      case Token.ASSIGN_SUB:
      case Token.ASSIGN_MUL:
      case Token.ASSIGN_DIV:
      case Token.ASSIGN_MOD:
        return true;
      case Token.FUNCTION:
        return false;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (!ControlFlowGraph.isEnteringNewCfgNode(c) && mayThrowException(c)) {
        return true;
      }
    }
    return false;
  }