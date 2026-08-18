  private void createSynthesizedExternVar(String varName) {
    Node nameNode = Node.newString(Token.NAME, varName);

    // Mark the variable as constant if it matches the coding convention
    // for constant vars.
    // NOTE(nicksantos): honestly, i'm not sure how much this matters.
    // AFAIK, all people who use the CONST coding convention also
    // compile with undeclaredVars as errors. We have some test
    // cases for this configuration though, and it makes them happier.
    if (compiler.getCodingConvention().isConstant(varName)) {
      nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    }

    Node externsRoot = getSynthesizedExternsRoot();
    // Check if the variable already exists in the externs root.
    boolean alreadyExists = false;
    for (Node child = externsRoot.getFirstChild(); child != null; child = child.getNext()) {
      if (child.getType() == Token.VAR) {
        Node firstChild = child.getFirstChild();
        if (firstChild != null && firstChild.getType() == Token.NAME && firstChild.getString().equals(varName)) {
          alreadyExists = true;
          break;
        }
      }
    }
    if (!alreadyExists) {
      externsRoot.addChildToBack(new Node(Token.VAR, nameNode));
    }
    varsToDeclareInExterns.remove(varName);
  }