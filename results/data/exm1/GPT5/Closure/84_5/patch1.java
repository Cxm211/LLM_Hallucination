Node processUnaryExpression(UnaryExpression exprNode) {
  int type = transformTokenType(exprNode.getType());
  Node operand = transform(exprNode.getOperand());
  if (type == Token.NEG && operand.getType() == Token.NUMBER) {
    // Create a new number node to avoid mutating a potentially shared node.
    return newNode(Token.NUMBER).setDouble(-operand.getDouble());
  } else {
    Node node = newNode(type, operand);
    if (exprNode.isPostfix()) {
      node.putBooleanProp(Node.INCRDECR_PROP, true);
    }
    return node;
  }
}
