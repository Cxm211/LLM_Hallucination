Node processUnaryExpression(UnaryExpression exprNode) {
  int type = transformTokenType(exprNode.getType());
  if (type == Token.DEC || type == Token.INC) {
    Node node = newNode(type, exprNode.getOperand());
    if (exprNode.isPostfix()) {
      node.putBooleanProp(Node.INCRDECR_PROP, true);
    }
    return node;
  }
  Node operand = transform(exprNode.getOperand());
  if (type == Token.NEG && operand.getType() == Token.NUMBER) {
    operand.setDouble(-operand.getDouble());
    return operand;
  } else {
    Node node = newNode(type, operand);
    if (exprNode.isPostfix()) {
      node.putBooleanProp(Node.INCRDECR_PROP, true);
    }
    return node;
  }
}