private boolean isPrototypePropertyAssign(Node assign) {
  if (assign == null) {
    return false;
  }
  if (!assign.isAssign()) {
    return false;
  }
  Node n = assign.getFirstChild();
  if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
      && n.isGetProp()
      && assign.getParent() != null
      && assign.getParent().isExprResult()) {
    boolean isChainedProperty =
        n.getFirstChild().isGetProp();

    if (isChainedProperty) {
      Node child = n.getFirstChild().getFirstChild().getNext();

      if (child != null && child.isString() &&
          child.getString().equals("prototype")) {
        return true;
      }
    }
  }
  return false;
}