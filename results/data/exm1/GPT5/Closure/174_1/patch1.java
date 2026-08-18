  public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      result = IR.var(nameNode, value);
      // Attach JSDoc to the declared name, not the VAR node.
      nameNode.setJSDocInfo(info);
    } else {
      Node assign = IR.assign(nameNode, value);
      result = IR.exprResult(assign);
      // Attach JSDoc to the qualified name being assigned, not the ASSIGN node.
      nameNode.setJSDocInfo(info);
    }
    return result;
  }