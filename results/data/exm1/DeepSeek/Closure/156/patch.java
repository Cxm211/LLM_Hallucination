private void collapseDeclarationOfNameAndDescendants(Name n, String alias) {
    boolean canCollapseChildNames = n.canCollapseUnannotatedChildNames();

    // Handle this name first so that nested object literals get unrolled.
    if (n.canCollapse() && canCollapseChildNames) {
      updateObjLitOrFunctionDeclaration(n, alias);
    }

    if (n.props != null) {
      for (Name p : n.props) {
        // Skip getter and setter properties, they should not be collapsed.
        if (p.declaration != null && p.declaration.node != null) {
          Node declNode = p.declaration.node;
          Node parent = declNode.getParent();
          // Check if this is a getter/setter in an object literal.
          if (parent != null && (parent.getType() == Token.GETTER_DEF || parent.getType() == Token.SETTER_DEF)) {
            continue;
          }
        }

        // Recurse first so that saved node ancestries are intact when needed.
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