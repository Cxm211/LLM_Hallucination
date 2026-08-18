private void createSynthesizedExternVar(String varName) {
    Node nameNode = Node.newString(Token.NAME, varName);

    if (compiler.getCodingConvention().isConstant(varName)) {
      nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    }

    Node varNode = new Node(Token.VAR, nameNode);
    Node externsRoot = getSynthesizedExternsRoot();
    if (!externsRoot.hasChildThatMatches(varNode)) {
      externsRoot.addChildToBack(varNode);
    }
    varsToDeclareInExterns.remove(varName);
  }