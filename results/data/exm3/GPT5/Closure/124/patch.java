private boolean isSafeReplacement(Node node, Node replacement) {
  // No checks are needed for simple names.
  if (node.isName()) {
    return true;
  }
  Preconditions.checkArgument(node.isGetProp());

  // Walk down the GETPROP chain to find the root name (e.g., x in x.a.b).
  Node current = node;
  while (current.isGetProp()) {
    current = current.getFirstChild();
  }

  if (current.isName() && isNameAssignedTo(current.getString(), replacement)) {
    return false;
  }

  return true;
}