private boolean isPrototypePropertyAssign(Node assign) {
  Node n = assign.getFirstChild();
  if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
      && n.getType() == Token.GETPROP) {
    // Check for direct prototype assignment (e.g., Foo.prototype = ...)
    if (n.getLastChild() != null && n.getLastChild().getType() == Token.STRING
        && n.getLastChild().getString().equals("prototype")) {
      return true;
    }
    // Check for chained prototype property assignment (e.g., Foo.prototype.bar = ...)
    if (n.getFirstChild().getType() == Token.GETPROP) {
      Node child = n.getFirstChild().getFirstChild().getNext();
      if (child != null && child.getType() == Token.STRING &&
          child.getString().equals("prototype")) {
        return true;
      }
    }
  }
  return false;
}