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
      if (declarationRoot.getType() == Token.FUNCTION ||
          declarationRoot.getType() == Token.CATCH) {
        renamer = nameStack.peek();
      } else {
        renamer = nameStack.peek().forChildScope();
      }
    }

    if (declarationRoot.getType() == Token.FUNCTION) {
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
    } else if (declarationRoot.getType() != Token.FUNCTION) {
      findDeclaredNames(declarationRoot, null, renamer);
    }

    if (nameStack.isEmpty() || 
        (declarationRoot.getType() != Token.FUNCTION &&
         declarationRoot.getType() != Token.CATCH)) {
      nameStack.push(renamer);
    }
  }