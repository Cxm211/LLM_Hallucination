private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Report ONLY if this 'this' is on the left-hand side of an assignment.
      for (Node anc = n; anc != null; anc = anc.getParent()) {
        if (anc == assignLhsChild) {
          return true;
        }
        if (anc.getType() == Token.ASSIGN) {
          break;
        }
      }
    }

    // Also report a THIS with a property access.
    int pType = parent.getType();
    return pType == Token.GETPROP || pType == Token.GETELEM;
  }