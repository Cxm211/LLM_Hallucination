private boolean isFoldableExpressBlock(Node n) {
  if (n.getType() == Token.BLOCK) {
    if (n.hasOneChild()) {
      Node maybeExpr = n.getFirstChild();
      if (NodeUtil.isExpressionNode(maybeExpr)) {
        Node expr = maybeExpr.getFirstChild();
        if (expr != null && expr.getType() == Token.CALL) {
          Node callee = expr.getFirstChild();
          if (callee != null && callee.getType() == Token.GETPROP) {
            Node propNode = callee.getLastChild();
            if (propNode != null && propNode.getType() == Token.STRING) {
              String propName = propNode.getString();
              if (propName.startsWith("on")) {
                return false;
              }
            }
          }
          if (callee != null && callee.getType() == Token.GETELEM) {
            return false;
          }
        }
        return true;
      }
    }
  }
  return false;
}