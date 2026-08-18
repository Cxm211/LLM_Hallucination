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

      // Only allow redeclaration of variables that were originally declared with 'var'.
      // Do not redeclare function parameters or other kinds of bindings.
      Node varNode = var.getNode();
      Node parent = varNode != null ? varNode.getParent() : null;
      if (parent == null || !parent.isVar()) {
        return false;
      }

      return true;
    }