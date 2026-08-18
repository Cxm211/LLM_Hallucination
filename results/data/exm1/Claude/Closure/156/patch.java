private void collapseDeclarationOfNameAndDescendants(Name n, String alias) {
    boolean canCollapseChildNames = n.canCollapseUnannotatedChildNames();

    if (n.canCollapse() && canCollapseChildNames) {
      updateObjLitOrFunctionDeclaration(n, alias);
    }

    if (n.props != null) {
      for (Name p : n.props) {
        collapseDeclarationOfNameAndDescendants(
            p, appendPropForAlias(alias, p.name));

        if (!p.inExterns && canCollapseChildNames && p.declaration != null &&
            p.declaration.node != null &&
            p.declaration.node.getParent() != null &&
            p.declaration.node.getParent().getType() == Token.ASSIGN) {
          updateSimpleDeclaration(
              appendPropForAlias(alias, p.name), p, p.declaration);
        }
      }
    }
  }