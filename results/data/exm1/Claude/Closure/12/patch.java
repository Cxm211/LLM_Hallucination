private boolean hasExceptionHandler(Node cfgNode) {
  for (Node n = cfgNode; n != null; n = n.getParent()) {
    if (n.isBlock() && n.getParent() != null) {
      Node parent = n.getParent();
      if (parent.isTry() && parent.getLastChild() == n) {
        return true;
      }
    }
    if (n.isCatch()) {
      return true;
    }
  }
  return false;
}