public static Node newQualifiedNameNodeDeclaration(
      CodingConvention convention, String name, Node value, JSDocInfo info) {
    Node result;
    Node nameNode = newQualifiedNameNode(convention, name);
    if (nameNode.isName()) {
      result = IR.var(nameNode, value);
      result.setJSDocInfo(info);
    } else {
      if (value == null) {
        // Just a declaration for a qualified name (e.g. /** @type {...} */ $jscomp.scope.a;)
        result = IR.exprResult(nameNode);
        nameNode.setJSDocInfo(info);
      } else {
        Node assign = IR.assign(nameNode, value);
        result = IR.exprResult(assign);
        // Attach JSDoc to the LHS of the assignment so that the type is applied to the name.
        assign.getFirstChild().setJSDocInfo(info);
      }
    }
    return result;
  }