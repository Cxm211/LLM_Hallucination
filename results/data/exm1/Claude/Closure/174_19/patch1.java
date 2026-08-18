public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      result = IR.var(nameNode, value);
      result.setJSDocInfo(info);
    } else {
      Node assignment = IR.assign(nameNode, value);
      assignment.setJSDocInfo(info);
      result = IR.exprResult(assignment);
    }
    return result;
  }