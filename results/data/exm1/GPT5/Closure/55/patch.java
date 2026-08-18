  private static boolean isReduceableFunctionExpression(Node n) {
    if (!NodeUtil.isFunctionExpression(n)) {
      return false;
    }
    Node parent = n.getParent();
    if (parent != null) {
      int type = parent.getType();
      if (type == Token.GETTER_DEF || type == Token.SETTER_DEF) {
        return false;
      }
    }
    return true;
  }