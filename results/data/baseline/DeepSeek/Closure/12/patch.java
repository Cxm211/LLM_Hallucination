private boolean hasExceptionHandler(Node cfgNode) {
    for (Node n = cfgNode; n != null; n = n.getParent()) {
      if (n.getType() == Token.TRY) {
        return true;
      }
    }
    return false;
  }