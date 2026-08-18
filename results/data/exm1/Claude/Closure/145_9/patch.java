private boolean isOneExactlyFunctionOrDo(Node n) {
    if (n.getType() == Token.BLOCK) {
      if (n.getChildCount() == 1) {
        return isOneExactlyFunctionOrDo(n.getFirstChild());
      } else {
        return false;
      }
    }
    return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }