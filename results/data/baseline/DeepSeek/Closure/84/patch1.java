    Node processUnaryExpression(UnaryExpression exprNode) {
      int type = transformTokenType(exprNode.getType());
      Node operand = transform(exprNode.getOperand());
      if (type == Token.NEG && operand.getType() == Token.NUMBER) {
        Node newNumber = Node.newNumber(-operand.getDouble());
        newNumber.copyInformationFrom(operand);
        return newNumber;
      } else {
        Node node = newNode(type, operand);
        if (exprNode.isPostfix()) {
          node.putBooleanProp(Node.INCRDECR_PROP, true);
        }
        return node;
      }
    }