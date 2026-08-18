  private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped.
        // Only return true when the node is a BLOCK with exactly one child
        // and that single child is a FUNCTION or DO.
        if (n.getType() != Token.BLOCK) {
          return false;
        }
        Node onlyChild = n.getFirstChild();
        if (onlyChild == null || onlyChild.getNext() != null) {
          return false;
        }
        int type = onlyChild.getType();
        return type == Token.FUNCTION || type == Token.DO;
  }