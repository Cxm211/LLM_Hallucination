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
      // Add recursive function name for function expressions
      String fnName = declarationRoot.getFirstChild().getString();
      if (fnName != null && !fnName.isEmpty() && !NodeUtil.isFunctionDeclaration(declarationRoot)) {
        renamer.addDeclaredName(fnName);
      }
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
    } else {
      // Add the block declarations
      if (declarationRoot.getType() == Token.CATCH) {
        Node catchVar = declarationRoot.getFirstChild();
        renamer.addDeclaredName(catchVar.getString());
      }
      findDeclaredNames(declarationRoot, null, renamer);
    }
    nameStack.push(renamer);
  }