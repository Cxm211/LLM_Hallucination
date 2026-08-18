private boolean isSafeReplacement(Node node, Node replacement) {
    if (node.isName()) {
      return true;
    }
    Preconditions.checkArgument(node.isGetProp());
    Node current = node;
    while (current.isGetProp()) {
      Node child = current.getFirstChild();
      if (child.isName() && isNameAssignedTo(child.getString(), replacement)) {
        return false;
      }
      current = child;
    }
    return true;
  }