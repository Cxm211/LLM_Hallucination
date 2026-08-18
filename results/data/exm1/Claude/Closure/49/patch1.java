public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    switch (n.getType()) {
      case Token.FUNCTION:
        {
          // NOTE: enterScope will handle adding parameters and body declarations
          // We only need to handle the recursive function name here if it's not a declaration
          if (parent != null && !NodeUtil.isFunctionDeclaration(n)) {
            Renamer renamer = nameStack.peek().forChildScope();
            String name = n.getFirstChild().getString();
            if (name != null && !name.isEmpty()) {
              renamer.addDeclaredName(name);
            }
            nameStack.push(renamer);
          }
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