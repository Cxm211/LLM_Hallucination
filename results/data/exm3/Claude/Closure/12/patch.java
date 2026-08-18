private boolean hasExceptionHandler(Node cfgNode) {
  // Check if this node is within a try block that has a catch handler
  for (Node n = cfgNode; n != null; n = n.getParent()) {
    if (n.isTry()) {
      // A try node with a catch block has at least 2 children:
      // child 0: try block
      // child 1: catch block (if present)
      // child 2: finally block (if present)
      if (n.getChildCount() >= 2 && n.getSecondChild() != null && !n.getSecondChild().isEmpty()) {
        return true;
      }
    }
  }
  return false;
}