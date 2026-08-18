  private boolean hasExceptionHandler(Node cfgNode) {
    Node n = cfgNode;
    while (n != null) {
      if (n.getType() == Token.TRY) {
        return true;
      }
      n = n.getParent();
    }
    return false;
  }