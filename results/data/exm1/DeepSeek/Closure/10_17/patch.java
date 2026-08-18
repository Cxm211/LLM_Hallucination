  static boolean mayBeString(Node n, boolean recurse) {
    if (n.getType() == Token.STRING) {
      return true;
    }
    if (recurse) {
      return allResultsMatch(n, MAY_BE_STRING_PREDICATE);
    } else {
      return mayBeStringHelper(n);
    }
  }