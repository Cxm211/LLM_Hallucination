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
    // delete operator returns a boolean.
    case Token.DELPROP:
    // Logical operators
    case Token.AND:
    case Token.OR:
      return true;
    case Token.COMMA:
      return isBooleanResultHelper(n.getLastChild());
    case Token.ASSIGN:
      return isBooleanResultHelper(n.getLastChild());
    case Token.HOOK:
      return isBooleanResultHelper(n.getFirstChild().getNext())
          && isBooleanResultHelper(n.getLastChild());
    default:
      return false;
  }
}