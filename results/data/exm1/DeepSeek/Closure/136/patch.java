private void addPossibleSignature(String name, Node node, NodeTraversal t) {
    boolean signatureAdded = false;
    if (node.getType() == Token.FUNCTION) {
      addSignature(name, node, t.getSourceName());
      signatureAdded = true;
    } else if (node.getType() == Token.NAME) {
      String functionName = node.getString();
      Scope.Var v = t.getScope().getVar(functionName);
      if (v == null) {
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