  private static boolean isReduceableFunctionExpression(Node n) {
    return NodeUtil.isFunctionExpression(n) &&
           n.getParent().getType() != Token.GET &&
           n.getParent().getType() != Token.SET;
  }