  private boolean isSafeReplacement(Node node, Node replacement) {
    // No checks are needed for simple names.
    if (node.isName()) {
      return true;
    }
    Preconditions.checkArgument(node.isGetProp());

    Node child = node.getFirstChild();
    while (child.isGetProp()) {
      child = child.getFirstChild();
    }
    if (child.isName()
        && isNameAssignedTo(child.getString(), replacement)) {
      return false;
    }

    return true;
  }