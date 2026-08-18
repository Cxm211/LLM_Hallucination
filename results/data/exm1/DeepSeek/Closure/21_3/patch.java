  public void visit(NodeTraversal t, Node n, Node parent) {
    if (n.isEmpty() ||
        n.isComma()) {
      return;
    }

    if (parent == null) {
      return;
    }

    if (n.isExprResult()) {
      return;
    }

    if (n.isQualifiedName() && n.getJSDocInfo() != null) {
      return;
    }

    boolean isResultUsed = NodeUtil.isExpressionResultUsed(n);
    boolean isSimpleOp = NodeUtil.isSimpleOperatorType(n.getType());
    
    if (parent.getType() == Token.COMMA) {
      if (isResultUsed) {
        return;
      }
      // Find the effective parent by skipping comma ancestors
      Node effectiveParent = parent;
      while (effectiveParent.getType() == Token.COMMA) {
        effectiveParent = effectiveParent.getParent();
      }
      // Check if the effective parent indicates a statement-level context
      if (effectiveParent.getType() == Token.EXPR_RESULT || effectiveParent.getType() == Token.BLOCK) {
        // proceed
      } else if (effectiveParent.getType() == Token.FOR && effectiveParent.getChildCount() == 4) {
        // Determine the direct child of effectiveParent that is in the comma chain
        Node child = n;
        while (child.getParent() != effectiveParent) {
          child = child.getParent();
        }
        // If the child is the init or increment, consider it statement-level
        if (child == effectiveParent.getFirstChild() || child == effectiveParent.getFirstChild().getNext().getNext()) {
          // proceed
        } else {
          return;
        }
      } else {
        return;
      }
    } else if (parent.getType() != Token.EXPR_RESULT && parent.getType() != Token.BLOCK) {
      if (! (parent.getType() == Token.FOR && parent.getChildCount() == 4 && (n == parent.getFirstChild() || n == parent.getFirstChild().getNext().getNext()))) {
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