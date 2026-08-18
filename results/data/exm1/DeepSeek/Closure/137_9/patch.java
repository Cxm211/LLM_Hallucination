private void findDeclaredNames(Node n, Node parent, Renamer renamer) {
    if (parent == null || parent.getType() != Token.FUNCTION || n == parent.getFirstChild()) {
      if (NodeUtil.isVarDeclaration(n)) {
        renamer.addDeclaredName(n.getString());
      } else if (NodeUtil.isFunctionDeclaration(n)) {
        Node nameNode = n.getFirstChild();
        renamer.addDeclaredName(nameNode.getString());
      }
      if (n.getType() != Token.FUNCTION) {
        for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
          findDeclaredNames(c, n, renamer);
        }
      }
    }
  }