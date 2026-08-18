    public void onRedeclaration(
        Scope s, String name,
        Node n, Node parent, Node gramps, Node nodeWithLineNumber) {

      // Don't allow multiple variables to be declared at the top level scope
      if (scope.isGlobal()) {
        Scope.Var origVar = scope.getVar(name);
        Node origParent = origVar.getParentNode();
        // Check for extern/source duplicate
        CompilerInput newInput = compiler.getInput(sourceName);
        if (newInput != null && origVar.input != null &&
            compiler.isExtern(newInput) != compiler.isExtern(origVar.input)) {
          // One is from externs, the other from source: allow duplicate.
          return;
        }
        if (origParent.getType() == Token.CATCH &&
            parent.getType() == Token.CATCH) {
          // Okay, both are 'catch(x)' variables.
          return;
        }

        boolean allowDupe = false;
        JSDocInfo info = n.getJSDocInfo();
        if (info == null) {
          info = parent.getJSDocInfo();
        }
        allowDupe =
            info != null && info.getSuppressions().contains("duplicate");

        if (!allowDupe) {
          compiler.report(
              JSError.make(sourceName, n,
                           VAR_MULTIPLY_DECLARED_ERROR,
                           name,
                           (origVar.input != null
                            ? origVar.input.getName()
                            : "??")));
        }
      } else if (name.equals(ARGUMENTS) && !NodeUtil.isVarDeclaration(n)) {
        // Disallow shadowing "arguments" as we can't handle with our current
        // scope modeling.
        compiler.report(
            JSError.make(sourceName, n,
                VAR_ARGUMENTS_SHADOWED_ERROR));
      }
    }