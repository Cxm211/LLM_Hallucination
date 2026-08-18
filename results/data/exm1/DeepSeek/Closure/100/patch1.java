  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    if (parent != null) {
      int parentType = parent.getType();
      if (parentType == Token.GETPROP || parentType == Token.GETELEM) {
        if (n == parent.getFirstChild()) {
          return true;
        }
      }
    }
    return false;
  }