private void updateObjLitOrFunctionDeclaration(Name n, String alias) {
    if (n.declaration == null) {
      return;
    }

    if (n.declaration.getTwin() != null) {
      return;
    }

    switch (n.declaration.node.getParent().getType()) {
      case Token.ASSIGN:
        updateObjLitOrFunctionDeclarationAtAssignNode(n, alias);
        break;
      case Token.VAR:
        updateObjLitOrFunctionDeclarationAtVarNode(n);
        break;
      case Token.FUNCTION:
        updateFunctionDeclarationAtFunctionNode(n);
        break;
    }
  }