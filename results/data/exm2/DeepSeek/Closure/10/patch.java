  static boolean mayBeString(Node n, boolean recurse) {
    if (n.getType() == Token.OR || n.getType() == Token.AND) {
      return mayBeStringHelper(n);
    }
    if (recurse) {
      return allResultsMatch(n, MAY_BE_STRING_PREDICATE);
    } else {
      return mayBeStringHelper(n);
    }
  }