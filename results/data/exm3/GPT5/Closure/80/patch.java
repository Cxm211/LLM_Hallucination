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
        return true;
      case Token.ASSIGN:
        // Result is boolean iff the assigned value is always boolean.
        return isBooleanResultHelper(n.getLastChild());
      case Token.COMMA:
        // Result is boolean iff the last expression is always boolean.
        return isBooleanResultHelper(n.getLastChild());
      case Token.AND:
      case Token.OR:
        // For logical ops, result is boolean iff both operands are always boolean.
        return isBooleanResultHelper(n.getFirstChild())
            && isBooleanResultHelper(n.getLastChild());
      case Token.HOOK: {
        // condition ? x : y => boolean iff both x and y are always boolean.
        Node thenBranch = n.getFirstChild().getNext();
        Node elseBranch = n.getLastChild();
        return isBooleanResultHelper(thenBranch) && isBooleanResultHelper(elseBranch);
      }
      default:
        return false;
    }
  }