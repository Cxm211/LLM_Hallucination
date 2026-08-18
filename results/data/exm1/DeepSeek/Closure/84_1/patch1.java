Node processUnaryExpression(UnaryExpression exprNode) {
  int type = transformTokenType(exprNode.getType());
  Node operand = transform(exprNode.getOperand());
  if (type == Token.NEG && operand.getType() == Token.NUMBER) {
    return newNumber(-operand.getDouble());
  } else {
    Node node = newNode(type, operand);
    if (type == Token.INC || type == Token.DEC) {
      node.putBooleanProp(Node.INCRDECR_PROP, true);
    }
    return node;
  }
}