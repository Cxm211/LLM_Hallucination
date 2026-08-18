private boolean canBeRedeclared(Node n, Scope s) {
  if (!NodeUtil.isDeclaration(n)) {
    return false;
  }
  Node lhs = n.getFirstChild();
  if (!lhs.isName()) {
    return false;
  }
  Var var = s.getVar(lhs.getString());
  return var != null
      && var.getScope() == s
      && !blacklistedVars.contains(var);
}