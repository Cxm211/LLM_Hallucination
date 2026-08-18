public void onRedeclaration(
        Scope s, String name, Node n, Node parent, Node gramps,
        Node nodeWithLineNumber) {
      Preconditions.checkState(n.getType() == Token.NAME);
      if (parent.getType() == Token.VAR) {
        // Handle single-child var by replacing with an assignment as before.
        if (parent.hasOneChild()) {
          // If name is "arguments", Var maybe null.
          replaceVarWithAssignment(n, parent, gramps);
        } else {
          // If the VAR has multiple children, remove the redeclared NAME from the
          // declaration list and, if it has an initializer, append an assignment
          // statement after the VAR.
          Node init = n.getFirstChild();
          parent.removeChild(n);

          if (init != null) {
            Node assign = new Node(Token.ASSIGN, n, init);
            Node expr = new Node(Token.EXPR_RESULT, assign);
            gramps.addChildAfter(expr, parent);
          }

          if (!parent.hasChildren()) {
            gramps.removeChild(parent);
          }
          reportCodeChange();
        }
      }
    }