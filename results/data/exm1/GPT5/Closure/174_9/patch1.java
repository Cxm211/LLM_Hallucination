  public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      result = IR.var(nameNode, value);
      if (info != null) {
        nameNode.setJSDocInfo(info);
      }
    } else {
      Node assign = IR.assign(nameNode, value);
      if (info != null) {
        nameNode.setJSDocInfo(info);
      }
      result = IR.exprResult(assign);
    }
    return result;
  }