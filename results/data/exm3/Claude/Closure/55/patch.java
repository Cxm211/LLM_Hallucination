private static boolean isReduceableFunctionExpression(Node n) {
  if (!NodeUtil.isFunctionExpression(n)) {
    return false;
  }
  Node parent = n.getParent();
  if (parent == null) {
    return true;
  }
  if (NodeUtil.isGetterOrSetter(parent)) {
    return false;
  }
  return true;
}

