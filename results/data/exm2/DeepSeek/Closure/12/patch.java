  private boolean hasExceptionHandler(Node cfgNode) {
    for (Node n = cfgNode; n != null; n = n.getParent()) {
      if (n.getType() == Token.TRY) {
        // The first child is the try block, the second is the catch block (if any)
        Node catchBlock = n.getFirstChild().getNext();
        return catchBlock != null;
      }
    }
    return false;
  }