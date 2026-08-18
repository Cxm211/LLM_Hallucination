  private boolean isSafeReplacement(Node node, Node replacement) {
    // No checks are needed for simple names.
    if (node.isName()) {
      return true;
    }
    // Allow both property access types.
    Preconditions.checkArgument(node.isGetProp() || node.isGetElem());

    // Walk down the property chain to find the root object.
    node = node.getFirstChild();
    while (node != null && (node.isGetProp() || node.isGetElem())) {
      node = node.getFirstChild();
    }

    if (node != null && node.isName()
        && isNameAssignedTo(node.getString(), replacement)) {
      return false;
    }

    return true;
  