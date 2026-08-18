static boolean mayBeString(Node n, boolean recurse) {
  if (recurse) {
    return mayBeStringHelper(n);
  } else {
    return allResultsMatch(n, MAY_BE_STRING_PREDICATE);
  }
}