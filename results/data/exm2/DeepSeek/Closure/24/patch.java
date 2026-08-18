private void findAliases(NodeTraversal t) {
  Scope scope = t.getScope();
  for (Var v : scope.getVarIterable()) {
    Node n = v.getNode();
    int type = n.getType();
    Node parent = n.getParent();
    if (parent.isVar()) {
      if (n.hasChildren() && n.getFirstChild().isQualifiedName()) {
        Node initialValue = n.getFirstChild();
        String qualifiedName = initialValue.getQualifiedName();
        int lastDotIndex = qualifiedName.lastIndexOf('.');
        String lastPart = lastDotIndex == -1 ? qualifiedName : qualifiedName.substring(lastDotIndex + 1);
        if (n.getString().equals(lastPart)) {
          String name = n.getString();
          Var aliasVar = scope.getVar(name);
          aliases.put(name, aliasVar);
          transformation.addAlias(name, qualifiedName);
          // Bleeding functions already get a BAD_PARAMETERS error, so just
          // do nothing.
          // Parameters of the scope function also get a BAD_PARAMETERS
          // error.
        } else {
          report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
        }
      } else {
        // TODO(robbyw): Support using locals for private variables.
        report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
      }
    } else {
      report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
    }
  }
}