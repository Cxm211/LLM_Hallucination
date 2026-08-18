static boolean isBooleanResultHelper(Node n) {
    switch (n.getType()) {
      case Token.TRUE:
      case Token.FALSE:
      case Token.EQ:
      case Token.NE:
      case Token.SHEQ:
      case Token.SHNE:
      case Token.LT:
      case Token.GT:
      case Token.LE:
      case Token.GE:
      case Token.IN:
      case Token.INSTANCEOF:
      case Token.NOT:
        return true;
      default:
        return false;
    }
  }