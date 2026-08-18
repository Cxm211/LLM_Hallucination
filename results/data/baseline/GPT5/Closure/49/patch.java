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

      if (declarationRoot.getType() == Token.FUNCTION) {
        for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
          String name = c.getString();
          renamer.addDeclaredName(name);
        }
        Node functionBody = declarationRoot.getLastChild();
        findDeclaredNames(functionBody, null, renamer);
      } else {
        // Add the block declarations
        findDeclaredNames(declarationRoot, null, renamer);
      }
      nameStack.push(renamer);
    } else {
      // Non-global scopes are handled in shouldTraverse/visit.
    }
  }