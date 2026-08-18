Node processUnaryExpression(UnaryExpression exprNode) {
  int type = transformTokenType(exprNode.getType());
  Node operand = transform(exprNode.getOperand());
  if (type == Token.NEG && operand.getType() == Token.NUMBER) {
    operand.setDouble(-operand.getDouble());
    return operand;
  } else {
    if ((type == Token.INC || type == Token.DEC) && operand.getType() == Token.CALL) {
      String msg = type == Token.INC ? "Invalid increment target" : "Invalid decrement target";
      errorReporter.error(msg, 
        sourceName, 
        operand.getLineno(), "", operand.getCharno());
    }
    Node node = newNode(type, operand);
    if (exprNode.isPostfix()) {
      node.putBooleanProp(Node.INCRDECR_PROP, true);
    }
    return node;
  }
}