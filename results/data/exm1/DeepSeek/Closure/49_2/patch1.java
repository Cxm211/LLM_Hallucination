public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    switch (n.getType()) {
      case Token.FUNCTION:
        {
          Renamer renamer = nameStack.peek().forChildScope();
          String name = n.getFirstChild().getString();
          if (name != null && !name.isEmpty() && parent != null
              && !NodeUtil.isFunctionDeclaration(n)) {
            renamer.addDeclaredName(name);
          }
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