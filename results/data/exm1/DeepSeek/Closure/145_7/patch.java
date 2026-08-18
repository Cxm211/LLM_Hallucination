  private boolean isOneExactlyFunctionOrDo(Node n) {
    // For labels with block children, we need to ensure that a
    // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
    // are skipped. 
    if (n.getType() == Token.BLOCK) {
      if (n.hasOneChild()) {
        return isOneExactlyFunctionOrDo(n.getFirstChild());
      } else {
        return false;
      }
    }
    if (n.getType() == Token.EMPTY) {
      return false;
    }
    return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }