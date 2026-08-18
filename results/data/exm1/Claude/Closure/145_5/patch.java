private boolean isOneExactlyFunctionOrDo(Node n) {
    if (n.getType() == Token.BLOCK) {
      if (n.hasChildren() && !n.hasMoreThanOneChild()) {
        return isOneExactlyFunctionOrDo(n.getFirstChild());
      } else {
        return false;
      }
    }
    return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }