    private void trySimplify(Node parent, Node node) {
      if (node.getType() != Token.EXPR_RESULT) {
        return;
      }

      Node exprBody = node.getFirstChild();
      if (exprBody == null) {
        return;
      }
      if (!NodeUtil.nodeTypeMayHaveSideEffects(exprBody)
      ) {
        changeProxy.replaceWith(parent, node, getSideEffectNodes(exprBody));
      }
    }