private boolean isFoldableExpressBlock(Node n) {
  if (n.getType() == Token.BLOCK) {
    if (n.hasOneChild()) {
      Node maybeExpr = n.getFirstChild();
      // IE has a bug where event handlers behave differently when
      // their return value is used vs. when their return value is in
      // an EXPR_RESULT. It's pretty freaking weird. See:
      // http://code.google.com/p/closure-compiler/issues/detail?id=291
      // We try to detect this case, and not fold EXPR_RESULTs
      // into other expressions.

        // We only have to worry about methods with an implicit 'this'
        // param, or this doesn't happen.

      if (NodeUtil.isExpressionNode(maybeExpr)) {
        Node expr = maybeExpr.getFirstChild();
        if (expr.getType() == Token.CALL) {
          Node name = expr.getFirstChild();
          if (name.getType() == Token.GETPROP) {
            String propName = name.getLastChild().getString();
            if (propName.startsWith("on")) {
              Node parent = n.getParent();
              if (parent != null && parent.getType() == Token.IF) {
                Node cond = parent.getFirstChild();
                if (!NodeUtil.isLiteralValue(cond, true)) {
                  return false;
                }
              } else {
                return false;
              }
            }
          } else if (name.getType() == Token.GETELEM) {
            // Computed property access: be conservative.
            Node parent = n.getParent();
            if (parent != null && parent.getType() == Token.IF) {
              Node cond = parent.getFirstChild();
              if (!NodeUtil.isLiteralValue(cond, true)) {
                return false;
              }
            } else {
              return false;
            }
          }
        }
        return true;
      }
    }
  }
  return false;
}