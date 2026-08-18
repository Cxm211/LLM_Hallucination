private boolean isOneExactlyFunctionOrDo(Node n) {
    if (n.getType() == Token.BLOCK) {
      if (n.hasOneChild()) {
        Node child = n.getFirstChild();
        return (child.getType() == Token.FUNCTION || child.getType() == Token.DO);
      }
      return false;
    }
    return (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }