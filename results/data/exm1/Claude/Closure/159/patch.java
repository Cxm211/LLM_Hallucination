private void findCalledFunctions(
    Node node, Set<String> changed) {
  Preconditions.checkArgument(changed != null);
  // For each referenced function, add a new reference
  if (node.getType() == Token.CALL) {
    Node child = node.getFirstChild();
    if (child.getType() == Token.NAME) {
      changed.add(child.getString());
    }
    // Process remaining children (skip first child as it's already processed)
    for (Node c = child.getNext(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
  } else {
    // For non-CALL nodes, process all children
    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
  }
}