Node processUnaryExpression(UnaryExpression exprNode) {
  int type = transformTokenType(exprNode.getType());
  Node operand = transform(exprNode.getOperand());
  Node node;
  if (type == Token.NEG && operand.getType() == Token.NUMBER) {
    operand.setDouble(-operand.getDouble());
    node = operand;
  } else {
    node = newNode(type, operand);
  }
  if (exprNode.isPostfix()) {
    node.putBooleanProp(Node.INCRDECR_PROP, true);
  }
  return node;
}