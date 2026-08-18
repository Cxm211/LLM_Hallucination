private void createSynthesizedExternVar(String varName) {
    Node nameNode = Node.newString(Token.NAME, varName);

    if (compiler.getCodingConvention().isConstant(varName)) {
      nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    }

    getSynthesizedExternsRoot().addChildToBack(
        new Node(Token.VAR, nameNode));
    compiler.reportCodeChange();
    varsToDeclareInExterns.remove(varName);
  }