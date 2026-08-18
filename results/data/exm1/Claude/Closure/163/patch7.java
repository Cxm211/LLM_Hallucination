private boolean processGlobalFunctionDeclaration(NodeTraversal t,
    Node nameNode, Node parent, Node gramps) {
  Node firstChild = nameNode.getFirstChild();

  if (
      isGlobalFunctionDeclaration(t, parent) ||
      firstChild != null &&
      isGlobalFunctionDeclaration(t, firstChild)) {
    String name = nameNode.getString();
    getNameInfoForName(name, VAR).getDeclarations().add(
        new GlobalFunction(nameNode, parent, gramps, t.getModule()));

    if (compiler.getCodingConvention().isExported(name) ||
        anchorUnusedVars) {
      addGlobalUseOfSymbol(name, t.getModule(), VAR);
    }

    return true;
  }
  return false;
}