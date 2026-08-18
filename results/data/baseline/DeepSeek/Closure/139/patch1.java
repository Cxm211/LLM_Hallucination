    public void onRedeclaration(
        Scope s, String name, Node n, Node parent, Node gramps,
        Node nodeWithLineNumber) {
      Preconditions.checkState(n.getType() == Token.NAME);
      if (parent.getType() == Token.VAR) {
        if (parent.hasOneChild()) {
          replaceVarWithAssignment(n, parent, gramps);
        }
      }
    }