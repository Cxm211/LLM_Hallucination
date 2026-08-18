public void onRedeclaration(
    Scope s, String name, Node n, Node parent, Node gramps,
    Node nodeWithLineNumber) {
  Preconditions.checkState(n.getType() == Token.NAME);
  if (parent.getType() == Token.VAR) {
    // If name is "arguments", Var maybe null.
    if (parent.hasOneChild()) {
      replaceVarWithAssignment(n, parent, gramps);
    } else {
      // Handle redeclaration when the VAR has multiple children.
      // Convert just this NAME declaration into an assignment (if it has an initializer)
      // and remove it from the VAR list, preserving the other declarations.
      Node nameNode = n;
      Node init = nameNode.getFirstChild();

      Node exprResult = null;
      if (init != null) {
        // Detach initializer and build assignment: name = init
        nameNode.removeChild(init);
        Node assign = new Node(Token.ASSIGN, nameNode, init);
        exprResult = new Node(Token.EXPR_RESULT, assign);
      }

      // Detach the name from the VAR declaration list.
      parent.removeChild(nameNode);

      // If we created an assignment expression, insert it after the VAR node.
      if (exprResult != null) {
        gramps.addChildAfter(exprResult, parent);
      }

      // If the VAR has no more children, remove it.
      if (!parent.hasChildren()) {
        gramps.removeChild(parent);
      }

      reportCodeChange("VAR child redeclaration");
    }
  }
}
