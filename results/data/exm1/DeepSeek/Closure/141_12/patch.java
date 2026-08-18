static boolean canBeSideEffected(Node n, Set<String> knownConstants) {
    switch (n.getType()) {
      case Token.CALL:
      case Token.NEW:
        return true;
      case Token.NAME:
        return !NodeUtil.isConstantName(n)
            && !knownConstants.contains(n.getString());
      case Token.GETPROP:
      case Token.GETELEM:
        return true;
      case Token.FUNCTION:
        return false;
    }

    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (canBeSideEffected(c, knownConstants)) {
        return true;
      }
    }

    return false;
  }