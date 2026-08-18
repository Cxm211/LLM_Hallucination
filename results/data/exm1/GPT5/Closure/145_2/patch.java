  private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped. 
        // Check if this node or (if it's a BLOCK) its single child is a FUNCTION or DO.
        if (n.getType() == Token.BLOCK) {
          if (n.hasOneChild()) {
            Node c = n.getFirstChild();
            return c != null && (c.getType() == Token.FUNCTION || c.getType() == Token.DO);
          }
          return false;
        }
        return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }