private static boolean isReduceableFunctionExpression(Node n) {
  if (!NodeUtil.isFunctionExpression(n)) {
    return false;
  }
  Node parent = n.getParent();
  if (parent != null && (parent.isGetterDef() || parent.isSetterDef())) {
    return false;
  }
  return true;
}