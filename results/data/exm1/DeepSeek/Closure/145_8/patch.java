  private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped. 
        // If the node is a block with a single child, check that child.
        if (n.getType() == Token.BLOCK) {
          Node child = n.getFirstChild();
          if (child != null && child.getNext() == null) {
            n = child;
          } else {
            // Either an empty block or a block with more than one child,
            // so it isn't a FUNCTION or DO.
            return false;
          }
        }
        return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }