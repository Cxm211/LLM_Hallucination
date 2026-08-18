    public void onRedeclaration(
        Scope s, String name, Node n, Node parent, Node gramps,
        Node nodeWithLineNumber) {
      Preconditions.checkState(n.getType() == Token.NAME);
      if (parent.getType() == Token.VAR) {
        if (parent.hasOneChild()) {
          replaceVarWithAssignment(n, parent, gramps);
        } else {
          parent.removeChild(n);
          if (n.hasChildren()) {
            Node initializer = n.getFirstChild();
            n.removeChild(initializer);
            Node assign = new Node(Token.ASSIGN, n, initializer);
            Node expr = new Node(Token.EXPR_RESULT, assign);
            gramps.addChildBefore(expr, parent);
            reportCodeChange("split var");
          }
          if (!parent.hasChildren()) {
            gramps.removeChild(parent);
            reportCodeChange("remove empty var");
          } else {
            reportCodeChange("remove duplicate from var");
          }
        }
      }
    }