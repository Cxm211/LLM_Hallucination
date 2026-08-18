private boolean isOneExactlyFunctionOrDo(Node n) {
  // For labels with block children, ensure that removing extraneous BLOCKs
  // does not produce a labeled FUNCTION or DO. If n is a BLOCK with exactly
  // one child that is a FUNCTION or DO, treat it as such.
  if (n.getType() == Token.FUNCTION || n.getType() == Token.DO) {
    return true;
  }
  if (n.getType() == Token.BLOCK) {
    Node first = n.getFirstChild();
    // exactly one child and it's a FUNCTION or DO
    if (first != null && first == n.getLastChild()) {
      int t = first.getType();
      return t == Token.FUNCTION || t == Token.DO;
    }
  }
  return false;
}