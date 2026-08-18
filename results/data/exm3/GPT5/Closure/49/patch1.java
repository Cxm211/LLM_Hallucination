public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    switch (n.getType()) {
      case Token.FUNCTION:
        // No-op: scope handling is managed in enterScope/visit.
        break;

      case Token.CATCH:
        // No-op: scope handling is managed in enterScope/visit.
        break;
    }

    return true;
  }