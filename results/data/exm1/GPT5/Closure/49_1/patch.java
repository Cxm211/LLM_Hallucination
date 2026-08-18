public void enterScope(NodeTraversal t) {
    Node declarationRoot = t.getScopeRoot();
    // Non-global scopes (like function and catch) are managed in shouldTraverse/visit.
    if (!nameStack.isEmpty()) {
      return;
    }

    Renamer renamer;
    // Handle global scope initialization.
    Preconditions.checkState(
        declarationRoot.getType() != Token.FUNCTION ||
        !(rootRenamer instanceof ContextualRenamer));
    Preconditions.checkState(t.inGlobalScope());
    renamer = rootRenamer;

    if (declarationRoot.getType() == Token.FUNCTION) {
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
    } else if (declarationRoot.getType() != Token.FUNCTION) {
      // Add the block declarations
      findDeclaredNames(declarationRoot, null, renamer);
    }
    nameStack.push(renamer);
  }