private boolean isFoldableExpressBlock(Node n) {
    if (n.getType() == Token.BLOCK && n.hasOneChild()) {
      Node maybeExpr = n.getFirstChild();
      if (maybeExpr != null && maybeExpr.getType() == Token.EXPR_RESULT) {
        Node expr = maybeExpr.getFirstChild();
        if (expr != null && expr.getType() == Token.CALL) {
          Node callee = expr.getFirstChild();
          if (callee != null) {
            int calleeType = callee.getType();
            // Do not fold calls of the form obj[...](...), to be conservative
            if (calleeType == Token.GETELEM) {
              return false;
            }
            // Do not fold event handler method calls like obj.onchange(),
            // as IE treats them specially when used in expressions.
            if (calleeType == Token.GETPROP) {
              Node prop = callee.getLastChild();
              if (prop != null && prop.getType() == Token.STRING) {
                String propName = prop.getString();
                if (propName != null && propName.startsWith("on")) {
                  return false;
                }
              }
            }
          }
        }
        return true;
      }
    }
    return false;
  }