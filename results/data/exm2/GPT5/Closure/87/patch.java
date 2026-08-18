private boolean isFoldableExpressBlock(Node n) {
  if (n.getType() == Token.BLOCK) {
    if (n.hasOneChild()) {
      Node maybeExpr = n.getFirstChild();
      if (NodeUtil.isExpressionNode(maybeExpr)) {
        Node expr = maybeExpr.getFirstChild();
        if (expr != null && expr.getType() == Token.CALL) {
          Node callee = expr.getFirstChild();
          if (callee != null) {
            int calleeType = callee.getType();
            if (calleeType == Token.GETELEM) {
              return false;
            }
            if (calleeType == Token.GETPROP) {
              Node prop = callee.getLastChild();
              if (prop != null
                  && prop.getType() == Token.STRING
                  && prop.getString() != null
                  && prop.getString().startsWith("on")) {
                return false;
              }
            }
          }
        }
        return true;
      }
    }
  }
  return false;
}