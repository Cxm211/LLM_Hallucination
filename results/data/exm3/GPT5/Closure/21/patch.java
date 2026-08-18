public void visit(NodeTraversal t, Node n, Node parent) {
    // VOID nodes appear when there are extra semicolons at the BLOCK level.
    // Allow extra semicolons at the BLOCK level only.
    if (n.isEmpty()) {
      if (parent != null && parent.getType() == Token.BLOCK) {
        return;
      }
      // otherwise, allow further checks to report useless empty statements
    } else if (n.isComma()) {
      return;
    }

    if (parent == null) {
      return;
    }

    // Do not try to remove a block or an expr result. We already handle
    // these cases when we visit the child, and the peephole passes will
    // fix up the tree in more clever ways when these are removed.
    if (n.isExprResult()) {
      return;
    }

    // This no-op statement was there so that JSDoc information could
    // be attached to the name. This check should not complain about it.
    if (n.isQualifiedName() && n.getJSDocInfo() != null) {
      return;
    }

    boolean isResultUsed = NodeUtil.isExpressionResultUsed(n);
    boolean isSimpleOp = NodeUtil.isSimpleOperatorType(n.getType());
    if (parent.getType() == Token.COMMA) {
      if (isResultUsed) {
        return;
      }
      if (n == parent.getLastChild()) {
        for (Node an : parent.getAncestors()) {
          int ancestorType = an.getType();
          if (ancestorType == Token.COMMA) continue;
          if (ancestorType != Token.EXPR_RESULT && ancestorType != Token.BLOCK) return;
          else break;
        }
      }
    } else if (parent.getType() != Token.EXPR_RESULT && parent.getType() != Token.BLOCK) {
      if (! (parent.getType() == Token.FOR && parent.getChildCount() == 4 && (n == parent.getFirstChild() || n == parent.getFirstChild().getNext().getNext())) && !n.isEmpty()) {
        return;
      }
    }
    if (
        (isSimpleOp || !NodeUtil.mayHaveSideEffects(n, t.getCompiler()))) {
      String msg = "This code lacks side-effects. Is there a bug?";
      if (n.isString()) {
        msg = "Is there a missing '+' on the previous line?";
      } else if (isSimpleOp) {
        msg = "The result of the '" + Token.name(n.getType()).toLowerCase() +
            "' operator is not being used.";
      }

      t.getCompiler().report(
          t.makeError(n, level, USELESS_CODE_ERROR, msg));
      // TODO(johnlenz): determine if it is necessary to
      // try to protect side-effect free statements as well.
      if (!NodeUtil.isStatement(n)) {
        problemNodes.add(n);
      }
    }
  }