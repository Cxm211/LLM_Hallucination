  public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      result = IR.var(nameNode, value);
      // Attach JSDoc to the name node for VAR declarations.
      nameNode.setJSDocInfo(info);
    } else {
      Node assign = IR.assign(nameNode, value);
      result = IR.exprResult(assign);
      // Attach JSDoc to the assignment node for qualified names.
      assign.setJSDocInfo(info);
    }
    return result;
  }