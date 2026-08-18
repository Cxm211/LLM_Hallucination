public void visit(NodeTraversal t, Node n, Node parent) {
  if (isCallToScopeMethod(n)) {
    validateScopeCall(t, n, n.getParent());
  }
  if (t.getScopeDepth() < 2) {
    return;
  }
  int type = n.getType();
  Var aliasVar = null;
  if (type == Token.NAME) {
    String name = n.getString();
    Var lexicalVar = t.getScope().getVar(n.getString());
    if (lexicalVar != null && lexicalVar == aliases.get(name)) {
      aliasVar = lexicalVar;
    }
  }
  if (t.getScopeDepth() == 2) {
    if (aliasVar != null && NodeUtil.isLValue(n)) {
      if (aliasVar.getNode() == n) {
        aliasDefinitionsInOrder.add(n);
        return;
      } else {
        report(t, n, GOOG_SCOPE_ALIAS_REDEFINED, n.getString());
      }
    }
    if (type == Token.RETURN) {
      report(t, n, GOOG_SCOPE_USES_RETURN);
    } else if (type == Token.THIS) {
      report(t, n, GOOG_SCOPE_REFERENCES_THIS);
    } else if (type == Token.THROW) {
      report(t, n, GOOG_SCOPE_USES_THROW);
    }
  }
  if (t.getScopeDepth() >= 2) {
    if (aliasVar != null) {
      aliasUsages.add(new AliasedNode(aliasVar, n));
    }
    JSDocInfo info = n.getJSDocInfo();
    if (info != null) {
      for (Node node : info.getTypeNodes()) {
        fixTypeNode(node);
      }
    }
  }
}