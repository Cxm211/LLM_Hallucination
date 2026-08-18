  public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      if (value != null) {
        result = IR.var(nameNode, value);
      } else {
        result = IR.var(nameNode);
      }
      result.setJSDocInfo(info);
    } else {
      if (value != null) {
        result = IR.exprResult(IR.assign(nameNode, value));
        result.getFirstChild().setJSDocInfo(info);
      } else {
        // No value to assign; just create the qualified name expression.
        result = IR.exprResult(nameNode);
        // Attach JSDoc to the qualified name node itself in this case.
        nameNode.setJSDocInfo(info);
      }
    }
    return result;
  }