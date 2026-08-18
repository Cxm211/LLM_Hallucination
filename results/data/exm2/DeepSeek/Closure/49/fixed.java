// ===== FIXED com.google.javascript.jscomp.MakeDeclaredNamesUnique :: enterScope(NodeTraversal) [lines 76-96] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-49-fixed/src/com/google/javascript/jscomp/MakeDeclaredNamesUnique.java =====
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

    if (declarationRoot.getType() != Token.FUNCTION) {
      // Add the block declarations
      findDeclaredNames(declarationRoot, null, renamer);
    }
    nameStack.push(renamer);
  }

// ===== FIXED com.google.javascript.jscomp.MakeDeclaredNamesUnique :: shouldTraverse(NodeTraversal, Node, Node) [lines 106-156] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-49-fixed/src/com/google/javascript/jscomp/MakeDeclaredNamesUnique.java =====
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

          nameStack.push(renamer);
        }
        break;

      case Token.LP: {
          Renamer renamer = nameStack.peek().forChildScope();

          // Add the function parameters
          for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
            String name = c.getString();
            renamer.addDeclaredName(name);
          }

          // Add the function body declarations
          Node functionBody = n.getNext();
          findDeclaredNames(functionBody, null, renamer);

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

// ===== FIXED com.google.javascript.jscomp.MakeDeclaredNamesUnique :: visit(NodeTraversal, Node, Node) [lines 159-192] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-49-fixed/src/com/google/javascript/jscomp/MakeDeclaredNamesUnique.java =====
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
        nameStack.pop();
        // Remove function recursive name (if any).
        nameStack.pop();
        break;

      case Token.LP:
        // Note: The parameters and function body variables live in the
        // same scope, we introduce the scope when in the "shouldTraverse"
        // visit of LP, but remove it when when we exit the function above.
        break;

      case Token.CATCH:
        // Remove catch except name from the stack of names.
        nameStack.pop();
        break;
    }
  }
