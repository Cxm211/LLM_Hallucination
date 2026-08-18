private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped.
        // If it's directly a FUNCTION or DO, return true.
        int type = n.getType();
        if (type == Token.FUNCTION || type == Token.DO) {
          return true;
        }
        // If it's a BLOCK with exactly one child, and that child is
        // a FUNCTION or DO, then treat it as exactly one FUNCTION/DO.
        if (type == Token.BLOCK && n.hasOneChild()) {
          Node c = n.getFirstChild();
          int ctype = c.getType();
          return ctype == Token.FUNCTION || ctype == Token.DO;
        }
        // Otherwise, it's not exactly one FUNCTION or DO.
        return false;
  }