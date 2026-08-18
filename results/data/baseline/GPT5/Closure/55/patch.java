private static boolean isReduceableFunctionExpression(Node n) {
    return n != null && (NodeUtil.isFunctionExpression(n) || n.isArrowFunction());
  }