private boolean isOneExactlyFunctionOrDo(Node n) {
        // Returns true if n has exactly one child and that child is a FUNCTION or DO.
        if (n == null || !n.hasOneChild()) {
          return false;
        }
        Node c = n.getFirstChild();
        return c.getType() == Token.FUNCTION || c.getType() == Token.DO;
  }