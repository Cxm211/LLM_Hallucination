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
      String fnName = declarationRoot.getFirstChild().getString();
      if (fnName != null && !fnName.isEmpty() &&
          !NodeUtil.isFunctionDeclaration(declarationRoot)) {
        renamer.addDeclaredName(fnName);
      }
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild();
           c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
    } else {
      if (declarationRoot.getType() == Token.CATCH) {
        String name = declarationRoot.getFirstChild().getString();
        renamer.addDeclaredName(name);
      }
      findDeclaredNames(declarationRoot, null, renamer);
    }
    nameStack.push(renamer);
  }