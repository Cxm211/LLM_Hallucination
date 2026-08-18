private void createSynthesizedExternVar(String varName) {
    Node nameNode = Node.newString(Token.NAME, varName);

    if (compiler.getCodingConvention().isConstant(varName)) {
      nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    }

    Node varNode = new Node(Token.VAR, nameNode);
    nameNode.setParent(varNode);
    getSynthesizedExternsRoot().addChildToBack(varNode);
    varsToDeclareInExterns.remove(varName);
  }