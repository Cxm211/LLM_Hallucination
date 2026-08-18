private boolean isGlobalFunctionDeclaration(NodeTraversal t, Node n) {
  return t.inGlobalScope() &&
      (NodeUtil.isFunctionDeclaration(n) ||
       n.isFunction() &&
       n.getParent().isName());
}