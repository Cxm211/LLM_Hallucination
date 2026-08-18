  public void enterScope(NodeTraversal t) {
    Node declarationRoot = t.getScopeRoot();

    // If we're entering the global scope, initialize the root renamer and push it.
    if (nameStack.isEmpty()) {
      // If the contextual renamer is being used the starting context can not
      // be a function.
      Preconditions.checkState(
          declarationRoot.getType() != Token.FUNCTION ||
          !(rootRenamer instanceof ContextualRenamer));
      Preconditions.checkState(t.inGlobalScope());

      Renamer renamer = rootRenamer;
      // Add the declarations in the global/script scope.
      if (declarationRoot.getType() == Token.FUNCTION) {
        // Should not happen for the global scope, but guard just in case.
        Node functionBody = declarationRoot.getLastChild();
        findDeclaredNames(functionBody, null, renamer);
      } else {
        findDeclaredNames(declarationRoot, null, renamer);
      }
      nameStack.push(renamer);
      return;
    }

    // For function and catch scopes, the scope has already been created and
    // pushed in shouldTraverse. Just record the declared names here without
    // pushing a new scope.
    if (declarationRoot.getType() == Token.FUNCTION) {
      Renamer renamer = nameStack.peek();
      // Add function parameters.
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      // Add function body declarations.
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
      return;
    }

    if (declarationRoot.getType() == Token.CATCH) {
      // Catch scope is also handled in shouldTraverse; do not push again here.
      // The catch identifier has already been recorded there.
      return;
    }

    // Other block scopes: create and push a child scope and add block declarations.
    Renamer renamer = nameStack.peek().forChildScope();
    findDeclaredNames(declarationRoot, null, renamer);
    nameStack.push(renamer);
  }

  public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    switch (n.getType()) {
      case Token.FUNCTION:
        {
          // Add recursive function name, if needed.
          // NOTE: "enterScope" is called after we need to pick up this name.
          Renamer renamer = nameStack.peek().forChildScope();

          // If needed, add the function recursive name.
          String name = n.getFirstChild().getString();
          if (name != null && !name.isEmpty() && parent != null
              && !NodeUtil.isFunctionDeclaration(n)) {
            renamer.addDeclaredName(name);
          }



          // Add the function parameters

          // Add the function body declarations

          nameStack.push(renamer);
        }
        break;

      case Token.CATCH:
        {
          Renamer renamer = nameStack.peek().forChildScope();

          String name = n.getFirstChild().getString();
          renamer.addDeclaredName(name);

          nameStack.push(renamer);
        }
        break;
    }

    return true;
  }

  public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()) {
      case Token.NAME:
        String newName = getReplacementName(n.getString());
        if (newName != null) {
          Renamer renamer = nameStack.peek();
          if (renamer.stripConstIfReplaced()) {
            // TODO(johnlenz): Do we need to do anything about the javadoc?
            n.removeProp(Node.IS_CONSTANT_NAME);
          }
          n.setString(newName);
          t.getCompiler().reportCodeChange();
        }
        break;

      case Token.FUNCTION:
        // Remove the function body scope
        // Remove function recursive name (if any).
        nameStack.pop();
        break;

        // Note: The parameters and function body variables live in the
        // same scope, we introduce the scope when in the "shouldTraverse"
        // visit of LP, but remove it when when we exit the function above.

      case Token.CATCH:
        // Remove catch except name from the stack of names.
        nameStack.pop();
        break;
    }
  }