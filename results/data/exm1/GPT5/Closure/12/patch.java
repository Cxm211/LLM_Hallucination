  private boolean hasExceptionHandler(Node cfgNode) {
    Node child = cfgNode;
    for (Node n = cfgNode != null ? cfgNode.getParent() : null; n != null; n = n.getParent()) {
      if (n.getType() == Token.TRY) {
        Node tryBlock = n.getFirstChild();
        if (child == tryBlock) {
          // If there's any sibling after the try block, it's a catch or finally
          // which acts as an exception handler for code in the try block.
          if (tryBlock.getNext() != null) {
            return true;
          }
        }
      }
      child = n;
    }
    return false;
  }