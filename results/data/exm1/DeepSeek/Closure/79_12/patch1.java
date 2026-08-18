  private void createSynthesizedExternVar(String varName) {
    Node nameNode = NodeUtil.newQualifiedNameNode(
        compiler.getCodingConvention(), varName, null,
        NodeUtil.NAME_REFERENCE);
    if (compiler.getCodingConvention().isConstant(varName)) {
      nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    }
    getSynthesizedExternsRoot().addChildToBack(
        new Node(Token.VAR, nameNode));
    varsToDeclareInExterns.remove(varName);
  }