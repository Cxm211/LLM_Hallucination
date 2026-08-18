  private boolean hasExceptionHandler(Node cfgNode) {
    if (cfgNode == null) {
      return false;
    }
    for (Node n = cfgNode; n != null; n = n.getParent()) {
      if (n.getType() == Token.TRY) {
        Node tryBlock = n.getFirstChild();
        Node catchBlock = (tryBlock != null) ? tryBlock.getNext() : null;
        Node finallyBlock = (catchBlock != null) ? catchBlock.getNext() : null;
        boolean hasCatch = catchBlock != null && catchBlock.hasChildren();
        boolean hasFinally = finallyBlock != null && finallyBlock.hasChildren();
        if (hasCatch || hasFinally) {
          return true;
        }
      }
    }
    return false;
  }