  private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped. 
        // If n is a BLOCK, only return true when it has exactly one child
        // and that child is FUNCTION or DO.
        if (n.getType() == Token.BLOCK) {
          if (n.hasOneChild()) {
            Node c = n.getFirstChild();
            return c.getType() == Token.FUNCTION || c.getType() == Token.DO;
          }
          return false;
        }
        // Otherwise, return true only if n itself is FUNCTION or DO.
        return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }