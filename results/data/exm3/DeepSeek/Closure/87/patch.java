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
            // Check for event handlers: if the expression is an EXPR_RESULT that is a call
            // to a method that might be an event handler (property name starts with "on"),
            // or a computed property access, we avoid folding.
            if (maybeExpr.getType() == Token.EXPR_RESULT) {
              Node expr = maybeExpr.getFirstChild();
              if (expr != null && expr.getType() == Token.CALL) {
                Node callTarget = expr.getFirstChild();
                if (callTarget != null) {
                  if (callTarget.getType() == Token.GETPROP) {
                    Node propNameNode = callTarget.getSecondChild();
                    if (propNameNode != null && propNameNode.getType() == Token.STRING) {
                      String propName = propNameNode.getString();
                      if (propName.startsWith("on") && propName.length() > 2) {
                        // Likely an event handler, do not fold.
                        return false;
                      }
                    }
                  } else if (callTarget.getType() == Token.GETELEM) {
                    // Computed property access, cannot determine if event handler, avoid folding.
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