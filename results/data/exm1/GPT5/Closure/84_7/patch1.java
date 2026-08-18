Node processUnaryExpression(UnaryExpression exprNode) {
      int type = transformTokenType(exprNode.getType());
      Node operand = transform(exprNode.getOperand());
      if (type == Token.NEG && operand.getType() == Token.NUMBER) {
        // Do not mutate the existing operand node; create a new number node.
        Node negNumber = Node.newNumber(-operand.getDouble());
        negNumber.copyInformationFrom(operand);
        return negNumber;
      } else {

        Node node = newNode(type, operand);
        if (exprNode.isPostfix()) {
          node.putBooleanProp(Node.INCRDECR_PROP, true);
        }
        return node;
      }
    }