public void enterScope(NodeTraversal t) {
    Node declarationRoot = t.getScopeRoot();
    Renamer renamer;
    if (nameStack.isEmpty()) {
      Preconditions.checkState(
          declarationRoot.getType() != Token.FUNCTION ||
          !(rootRenamer instanceof ContextualRenamer));
      Preconditions.checkState(t.inGlobalScope());
      renamer = rootRenamer;
      nameStack.push(renamer);
      return;
    }

    if (declarationRoot.getType() == Token.FUNCTION) {
      // Use the already pushed renamer from shouldTraverse
      renamer = nameStack.peek();
      // Add function parameters
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild();
           c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      // Add function body declarations
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
      // No push; already on stack
    } else if (declarationRoot.getType() == Token.CATCH) {
      // Use the already pushed renamer from shouldTraverse
      renamer = nameStack.peek();
      // Add any var declarations inside the catch block
      findDeclaredNames(declarationRoot, null, renamer);
    } else {
      // Non-function, non-catch scope: create child scope and push
      renamer = nameStack.peek().forChildScope();
      findDeclaredNames(declarationRoot, null, renamer);
      nameStack.push(renamer);
    }
  }