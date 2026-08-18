private void trySimplify(Node parent, Node node) {
      if (node.getType() != Token.EXPR_RESULT) {
        return;
      }

      Node exprBody = node.getFirstChild();
      if (!NodeUtil.mayHaveSideEffects(exprBody)) {
        changeProxy.replaceWith(parent, node, getSideEffectNodes(exprBody));
      }
    }