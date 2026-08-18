private void visitFunction(NodeTraversal t, Node n) {
  FunctionType funType = (FunctionType) n.getJSType();
  Node block = n.getLastChild();
  Node paramName = NodeUtil.getFnParameters(n).getFirstChild();
  Node insertionPoint = null;

  for (Node child = block.getFirstChild(); child != null; child = child.getNext()) {
    if (child.getType() == Token.FUNCTION) {
      insertionPoint = child;
    }
  }

  for (Node paramType : funType.getParameters()) {
    if (paramName == null) {
      return;
    }

    Node checkNode = createCheckTypeCallNode(
        paramType.getJSType(), paramName.cloneTree());

    if (checkNode == null) {
      paramName = paramName.getNext();
      continue;
    }

    checkNode = new Node(Token.EXPR_RESULT, checkNode);
    if (insertionPoint == null) {
      block.addChildToFront(checkNode);
    } else {
      block.addChildAfter(checkNode, insertionPoint);
    }

    compiler.reportCodeChange();
    paramName = paramName.getNext();
    insertionPoint = checkNode;
  }
}