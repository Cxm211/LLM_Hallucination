Node processUnaryExpression(UnaryExpression exprNode) {
  int type = transformTokenType(exprNode.getType());
  Node operand = transform(exprNode.getOperand());
  if (type == Token.NEG && operand.getType() == Token.NUMBER) {
    operand.setDouble(-operand.getDouble());
    operand.copyInformationFrom(newNode(type));
    return operand;
  } else {
    Node node = newNode(type, operand);
    if (exprNode.isPostfix()) {
      node.putBooleanProp(Node.INCRDECR_PROP, true);
    }
    return node;
  }
}