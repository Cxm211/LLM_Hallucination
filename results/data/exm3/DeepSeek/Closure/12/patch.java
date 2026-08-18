  private boolean hasExceptionHandler(Node cfgNode) {
    Node parent = cfgNode.getParent();
    while (parent != null) {
      if (parent.isTry()) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }