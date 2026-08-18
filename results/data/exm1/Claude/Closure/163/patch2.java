public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
  if (isPrototypePropertyAssign(n)) {
    symbolStack.push(new NameContext(getNameInfoForName(
        n.getFirstChild().getLastChild().getString(), PROPERTY)));
  } else if (isGlobalFunctionDeclaration(t, n)) {
    String name = parent.isName() ?
        parent.getString() :
        n.getFirstChild().getString();
    symbolStack.push(new NameContext(getNameInfoForName(name, VAR)));
  } else if (n.isFunction()) {
    symbolStack.push(new NameContext(anonymousNode));
  }
  return true;
}