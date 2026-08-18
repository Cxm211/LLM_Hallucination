private boolean canBeRedeclared(Node n, Scope s) {
  if (!NodeUtil.isExprAssign(n)) {
    return false;
  }
  Node assign = n.getFirstChild();
  Node lhs = assign.getFirstChild();

  if (!lhs.isName()) {
    return false;
  }

  Var var = s.getVar(lhs.getString());
  if (var == null || var.getScope() != s || blacklistedVars.contains(var)) {
    return false;
  }
  Node declNode = var.getParentNode();
  if (declNode == null || !declNode.isVar()) {
    return false;
  }
  return true;
}