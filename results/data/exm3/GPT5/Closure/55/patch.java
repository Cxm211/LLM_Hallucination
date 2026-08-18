private static boolean isReduceableFunctionExpression(Node n) {
    // Only reduce plain function expressions that are not part of ES5 getter/setter definitions.
    if (!NodeUtil.isFunctionExpression(n)) {
      return false;
    }
    Node parent = n.getParent();
    if (parent != null && (parent.isGetterDef() || parent.isSetterDef())) {
      return false;
    }
    return true;
  }