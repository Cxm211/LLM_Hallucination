private void addPossibleSignature(String name, Node node, NodeTraversal t) {
    boolean signatureAdded = false;
    if (node.getType() == Token.FUNCTION) {
      // The node we're looking at is a function, so we can add it directly
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
      Node function = v.getInitialValue();
      // For function declarations (including externs), the initial value may be null.
      // In that case, the parent node can be the FUNCTION node.
      if (function == null) {
        Node parent = v.getParentNode();
        if (parent != null && parent.getType() == Token.FUNCTION) {
          function = parent;
        }
      }
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