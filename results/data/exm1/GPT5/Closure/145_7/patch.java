  private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped. 
        // Either a empty statement or an block with more than one child,
        // way it isn't a FUNCTION or DO.
        if (n.getType() == Token.BLOCK) {
          if (n.hasOneChild()) {
            Node child = n.getFirstChild();
            return child.getType() == Token.FUNCTION || child.getType() == Token.DO;
          }
          return false;
        }
        return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }