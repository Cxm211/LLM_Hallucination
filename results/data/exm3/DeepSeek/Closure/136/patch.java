  private void addPossibleSignature(String name, Node node, NodeTraversal t) {
    boolean signatureAdded = false;
    if (node.getType() == Token.FUNCTION) {
      // Check if source is externs
      if (compiler.isExternInput(t.getSourceName())) {
        return;
      }
      addSignature(name, node, t.getSourceName());
      signatureAdded = true;
    } else if (node.getType() == Token.NAME) {
      String functionName = node.getString();
      Scope.Var v = t.getScope().getVar(functionName);
      if (v == null) {
        if (compiler.isIdeMode()) {
          return;
        } else {
          throw new IllegalStateException(
              "VarCheck should have caught this undefined function");
        }
      }
      // Check if var is from externs
      if (compiler.isExternInput(v.getInputName())) {
        return;
      }
      Node function = v.getInitialValue();
      if (function != null &&
          function.getType() == Token.FUNCTION) {
        addSignature(name, function, v.getInputName());
        signatureAdded = true;
      }
    }
    if (!signatureAdded) {
      nonMethodProperties.add(name);
    }
  }