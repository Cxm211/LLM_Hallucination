  private void findDeclaredNames(Node n, Node parent, Renamer renamer) {
    // Do a shallow traversal, so don't traverse into function declarations,
    // except for the name of the function itself.
    if (NodeUtil.isVarDeclaration(n)) {
      renamer.addDeclaredName(n.getString());
    } else if (NodeUtil.isFunctionDeclaration(n)) {
      Node nameNode = n.getFirstChild();
      renamer.addDeclaredName(nameNode.getString());
    } else if (n.getType() == Token.NAME && parent != null && parent.getType() == Token.PARAM_LIST) {
      renamer.addDeclaredName(n.getString());
    }

    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      findDeclaredNames(c, n, renamer);
    }
  }