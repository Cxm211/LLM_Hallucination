  public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      result = IR.var(nameNode, value);
      result.setJSDocInfo(info);
    } else {
      result = IR.exprResult(IR.assign(nameNode, value));
      // Attach JSDoc to the qualified name node (lhs), not the assign node.
      result.getFirstChild().getFirstChild().setJSDocInfo(info);
    }
    return result;
  }