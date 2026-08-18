  public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      // For a simple name, create a var declaration and attach the initializer
      // to the NAME node (not as a sibling of VAR).
      result = IR.var(nameNode);
      if (value != null) {
        nameNode.addChildToFront(value);
      }
      // Attach JSDoc to the NAME node for correct var jsdoc semantics.
      nameNode.setJSDocInfo(info);
    } else {
      result = IR.exprResult(IR.assign(nameNode, value));
      result.getFirstChild().setJSDocInfo(info);
    }
    return result;
  }