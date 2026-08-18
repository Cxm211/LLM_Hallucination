Node processUnaryExpression(UnaryExpression exprNode) {
  int type = transformTokenType(exprNode.getType());
  Node operand = transform(exprNode.getOperand());
  Node node = newNode(type, operand);
  if (type == Token.INC || type == Token.DEC) {
    node.putBooleanProp(Node.INCRDECR_PROP, true);
  }
  return node;
}