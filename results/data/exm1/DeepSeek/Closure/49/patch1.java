public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()) {
      case Token.FUNCTION:
      case Token.CATCH:
        break;
      default:
    }
    return true;
  }