boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }

  // Make sure this assignment is not in a loop.
  Node node = ref.getNode();
  if (node == null) {
    return false;
  }
  Node parent = node.getParent();
  while (parent != null) {
    int type = parent.getType();
    if (type == Token.FOR || type == Token.WHILE || type == Token.DO) {
      return false;
    }
    parent = parent.getParent();
  }
  return true;
}