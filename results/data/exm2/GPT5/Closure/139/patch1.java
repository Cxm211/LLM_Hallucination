public void onRedeclaration(
        Scope s, String name, Node n, Node parent, Node gramps,
        Node nodeWithLineNumber) {
      Preconditions.checkState(n.getType() == Token.NAME);
      if (parent != null && parent.getType() == Token.VAR) {
      // If name is "arguments", Var maybe null.
        replaceVarWithAssignment(n, parent, gramps);
      }
    }