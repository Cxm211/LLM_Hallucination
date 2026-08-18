Node processUnaryExpression(UnaryExpression exprNode) {
      int type = transformTokenType(exprNode.getType());
      Node operand = transform(exprNode.getOperand());
      if (type == Token.NEG && operand.getType() == Token.NUMBER) {
        operand.setDouble(-operand.getDouble());
        return operand;
      } else {
        int nodeType = type;
        if (type == Token.DELPROP && operand.getType() == Token.NAME) {
          nodeType = Token.DELREF;
        }
        Node node = newNode(nodeType, operand);
        if ((nodeType == Token.INC || nodeType == Token.DEC) && exprNode.isPostfix()) {
          node.putBooleanProp(Node.INCRDECR_PROP, true);
        }
        return node;
      }
    }