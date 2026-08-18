Node processAssignment(Assignment assignmentNode) {
  int type = transformTokenType(assignmentNode.getType());
  Node left = transform(assignmentNode.getLeft());
  Node right = transform(assignmentNode.getRight());
  Node assign = newNode(type, left, right);
  return assign;
}

Node processUnaryExpression(UnaryExpression exprNode) {
  int type = transformTokenType(exprNode.getType());
  Node operand = transform(exprNode.getOperand());
  if ((type == Token.NEG || type == Token.POS) && operand.getType() == Token.NUMBER) {
    if (type == Token.NEG) {
      operand.setDouble(-operand.getDouble());
    }
    return operand;
  } else {
    Node node = newNode(type, operand);
    if (exprNode.isPostfix()) {
      node.putBooleanProp(Node.INCRDECR_PROP, true);
    }
    return node;
  }
}
