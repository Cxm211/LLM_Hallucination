  private boolean isOneExactlyFunctionOrDo(Node n) {
        // For labels with block children, we need to ensure that a
        // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
        // are skipped. 
        // Walk through LABEL and single-child BLOCK nodes to find the underlying statement.
        while (n != null && n.getType() == Token.LABEL) {
          n = n.getLastChild();
        }
        while (n != null && n.getType() == Token.BLOCK && n.hasOneChild()) {
          n = n.getFirstChild();
        }
        return n != null && (n.getType() == Token.FUNCTION || n.getType() == Token.DO);
  }