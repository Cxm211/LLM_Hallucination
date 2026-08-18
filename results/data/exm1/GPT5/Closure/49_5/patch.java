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
      // Push the root scope renamer.
      nameStack.push(renamer);
    }

    // For function and catch scopes, a child scope renamer is pushed in
    // shouldTraverse(). Do not push another here; just use the current top.
    if (declarationRoot.getType() == Token.FUNCTION) {
      renamer = nameStack.peek();
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild();
           c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
    } else if (declarationRoot.getType() != Token.FUNCTION) {
      // For non-function scopes (e.g., SCRIPT or BLOCK), introduce a new child scope.
      renamer = nameStack.peek().forChildScope();
      // Add the block declarations
      findDeclaredNames(declarationRoot, null, renamer);
      nameStack.push(renamer);
    }
  }