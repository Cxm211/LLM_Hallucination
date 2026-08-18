  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    if (parent != null && (parent.getType() == Token.GETPROP || parent.getType() == Token.GETELEM)) {
      return parent.getFirstChild() == n;
    }

    return false;
  }
