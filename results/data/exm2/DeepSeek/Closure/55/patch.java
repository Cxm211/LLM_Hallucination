  private static boolean isReduceableFunctionExpression(Node n) {
    if (!NodeUtil.isFunctionExpression(n)) {
      return false;
    }
    Node parent = n.getParent();
    if (parent != null && 
        (parent.getType() == Token.GETTER_DEF ||
         parent.getType() == Token.SETTER_DEF)) {
      return false;
    }
    return true;
  }