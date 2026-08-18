private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs
        // are skipped. Walk through single-child BLOCKs to find the
        // underlying statement, and only return true if that statement is
        // a FUNCTION or DO.
        Node current = n;
        // If it's a BLOCK, it must have exactly one child to be considered.
        while (current.getType() == Token.BLOCK) {
          Node first = current.getFirstChild();
          // If there is not exactly one child, it's not exactly a FUNCTION or DO.
          if (first == null || first.getNext() != null) {
            return false;
          }
          current = first;
        }
        return (current.getType() == Token.FUNCTION || current.getType() == Token.DO);
  }