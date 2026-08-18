private void findAliases(NodeTraversal t) {
  Scope scope = t.getScope();
  for (Var v : scope.getVarIterable()) {
    Node n = v.getNode();
    Node parent = n.getParent();
    boolean isVar = parent.isVar();
    boolean isFunctionDecl = NodeUtil.isFunctionDeclaration(parent);
    if (isVar && n.getFirstChild() != null && n.getFirstChild().isQualifiedName()) {
      recordAlias(v);
    } else if (v.isBleedingFunction()) {
    } else if (parent.getType() == Token.LP) {
    } else if (isVar || isFunctionDecl) {
      boolean isHoisted = NodeUtil.isHoistedFunctionDeclaration(parent);
      Node grandparent = parent.getParent();
      Node value = v.getInitialValue() != null ? v.getInitialValue() : null;
      Node varNode = null;
      String name = n.getString();
      int nameCount = scopedAliasNames.count(name);
      scopedAliasNames.add(name);
      String globalName = "$jscomp.scope." + name + (nameCount == 0 ? "" : ("$" + nameCount));
      compiler.ensureLibraryInjected("base");
      if (isFunctionDecl) {
        Node existingName = v.getNameNode();
        Node newName = IR.name("").useSourceInfoFrom(existingName);
        value.replaceChild(existingName, newName);
        varNode = IR.var(existingName).useSourceInfoFrom(existingName);
        grandparent.replaceChild(parent, varNode);
      } else {
        if (value != null) {
          value.detachFromParent();
        }
        varNode = parent;
      }
      if (value != null || v.getJSDocInfo() != null) {
        Node newDecl = NodeUtil.newQualifiedNameNodeDeclaration(
            compiler.getCodingConvention(), globalName, value, v.getJSDocInfo())
            .useSourceInfoIfMissingFromForTree(n);
        NodeUtil.setDebugInformation(
            newDecl.getFirstChild().getFirstChild(), n, name);
        if (isHoisted) {
          grandparent.addChildToFront(newDecl);
        } else {
          grandparent.addChildBefore(newDecl, varNode);
        }
      }
      Node newInitialValue = NodeUtil.newQualifiedNameNode(
          compiler.getCodingConvention(), globalName, n, name);
      varNode.addChildToBack(newInitialValue);
      recordAlias(v);
    } else {
      report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
    }
  }
}