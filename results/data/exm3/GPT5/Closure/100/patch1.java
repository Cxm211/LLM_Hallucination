private boolean shouldReportThis(Node n, Node parent) {
    // Do not report when 'this' is the direct left-hand side of an assignment.
    if (assignLhsChild != null && n == assignLhsChild) {
      return false;
    }

    // Report a THIS with a property access: this.foo
    if (parent != null && parent.getType() == Token.GETPROP && parent.getFirstChild() == n) {
      return true;
    }

    return false;
  }