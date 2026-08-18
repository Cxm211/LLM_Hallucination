  static boolean isBooleanResultHelper(Node n) {
    switch (n.getType()) {
      // Primitives
      case Token.TRUE:
      case Token.FALSE:
      // Comparisons
      case Token.EQ:
      case Token.NE:
      case Token.SHEQ:
      case Token.SHNE:
      case Token.LT:
      case Token.GT:
      case Token.LE:
      case Token.GE:
      // Queryies
      case Token.IN:
      case Token.INSTANCEOF:
      // Inversion
      case Token.NOT:
      // Logical operators that return boolean
      case Token.AND:
      case Token.OR:
      case Token.HOOK:
        return true;
      // Comma returns the last child
      case Token.COMMA:
      // Assign returns the value of the RHS
      case Token.ASSIGN:
        return isBooleanResultHelper(n.getLastChild());
      case Token.DELETE:
        // delete operator returns a boolean.
        return true;
      default:
        return false;
    }
  }