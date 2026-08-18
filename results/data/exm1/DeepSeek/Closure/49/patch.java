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
      String name = declarationRoot.getFirstChild().getString();
      if (name != null && !name.isEmpty() && declarationRoot.getParent() != null
          && !NodeUtil.isFunctionDeclaration(declarationRoot)) {
        renamer.addDeclaredName(name);
      }
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
        String paramName = c.getString();
        renamer.addDeclaredName(paramName);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
    } else if (declarationRoot.getType() == Token.CATCH) {
      String name = declarationRoot.getFirstChild().getString();
      renamer.addDeclaredName(name);
    } else {
      findDeclaredNames(declarationRoot, null, renamer);
    }
    nameStack.push(renamer);
  }