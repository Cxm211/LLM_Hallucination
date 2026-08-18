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
      return var != null
          && var.getScope() == s
          && !blacklistedVars.contains(var);
    }

// trigger testcase
public void testIssue820() throws Exception {
    // Don't redeclare function parameters, this is incompatible with
    // strict mode.
    testSame("function f(a){ var b=1; a=2; var c; }");
  }
