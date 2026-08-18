private boolean canBeRedeclared(Node n, Scope s) {
  if (!(n.isAssign() || NodeUtil.isExprAssign(n))) {
    return false;
  }
  Node assign = n.isAssign() ? n : n.getFirstChild();
  Node lhs = assign.getFirstChild();

  if (lhs == null || !lhs.isName()) {
    return false;
  }

  Var var = s.getVar(lhs.getString());
  return var != null
      && var.getScope() == s
      && !blacklistedVars.contains(var);
}
