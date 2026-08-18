  public void enterScope(NodeTraversal t) {
    Node declarationRoot = t.getScopeRoot();
    Renamer renamer;
    if (nameStack.isEmpty()) {
      // If the contextual renamer is being used the starting context can not
      // be a function.
      Preconditions.checkState(
          declarationRoot.getType() != Token.FUNCTION ||
          !(rootRenamer instanceof ContextualRenamer));
      Preconditions.checkState(t.inGlobalScope());
      renamer = rootRenamer;
    } else {
      renamer = nameStack.peek().forChildScope();
    }

    if (declarationRoot.getType() == Token.FUNCTION) {
      // Add the function expression name, if any.
      if (!NodeUtil.isFunctionDeclaration(declarationRoot)) {
        String functionName = declarationRoot.getFirstChild().getString();
        if (functionName != null && !functionName.isEmpty()) {
          renamer.addDeclaredName(functionName);
        }
      }
      // Add the parameters
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
    } else if (declarationRoot.getType() == Token.CATCH) {
      // Add the catch variable
      String name = declarationRoot.getFirstChild().getString();
      renamer.addDeclaredName(name);
      // Add the block declarations
      findDeclaredNames(declarationRoot, null, renamer);
    } else {
      // Add the block declarations
      findDeclaredNames(declarationRoot, null, renamer);
    }
    nameStack.push(renamer);
  }