  public void enterScope(NodeTraversal t) {
    Node declarationRoot = t.getScopeRoot();
    Renamer renamer;
    if (nameStack.isEmpty()) {
      Preconditions.checkState(
          declarationRoot.getType() != Token.FUNCTION ||
          !(rootRenamer instanceof ContextualRenamer));
      Preconditions.checkState(t.inGlobalScope());
      renamer = rootRenamer;
    } else {
      renamer = nameStack.peek().forChildScope();
    }

    if (declarationRoot.getType() == Token.FUNCTION) {
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      String name = declarationRoot.getFirstChild().getString();
      if (name != null && !name.isEmpty() && declarationRoot.getParent() != null
          && !NodeUtil.isFunctionDeclaration(declarationRoot)) {
        renamer.addDeclaredName(name);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
      nameStack.push(renamer);
    } else if (declarationRoot.getType() == Token.CATCH) {
      String name = declarationRoot.getFirstChild().getString();
      renamer.addDeclaredName(name);
      Node catchBlock = declarationRoot.getLastChild();
      findDeclaredNames(catchBlock, null, renamer);
      nameStack.push(renamer);
    } else {
      findDeclaredNames(declarationRoot, null, renamer);
    }
  }