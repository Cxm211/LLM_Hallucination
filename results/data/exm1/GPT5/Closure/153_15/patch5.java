
    public void onRedeclaration(
        Scope s, String name,
        Node n, Node parent, Node gramps, Node nodeWithLineNumber) {

      // Allow multiple variables to be declared at the top level scope.
      if (s.isGlobal()) {
        // Special-case: if both are catch variables, allow.
        Scope.Var origVar = s.getVar(name);
        if (origVar != null) {
          Node origParent = origVar.getParentNode();
          if (origParent.getType() == Token.CATCH &&
              parent.getType() == Token.CATCH) {
            return;
          }
        }
        return;
      } else if (name.equals(ARGUMENTS) && !NodeUtil.isVarDeclaration(n)) {
        // Disallow shadowing "arguments" as we can't handle with our current
        // scope modeling.
        compiler.report(
            JSError.make(sourceName, n,
                VAR_ARGUMENTS_SHADOWED_ERROR));
      }
    }