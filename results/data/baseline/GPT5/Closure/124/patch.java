  private boolean isSafeReplacement(Node node, Node replacement) {
    // No checks are needed for simple names.
    if (node.isName()) {
      return true;
    }
    Preconditions.checkArgument(node.isGetProp() || node.isGetElem());

      node = node.getFirstChild();
    if (node.isName()
        && isNameAssignedTo(node.getString(), replacement)) {
      return false;
    }

    return true;
  }