Node processUnaryExpression(UnaryExpression exprNode) {
      int type = transformTokenType(exprNode.getType());
      Node operand = transform(exprNode.getOperand());
      if (type == Token.NEG && operand.getType() == Token.NUMBER) {
        operand.setDouble(-operand.getDouble());
        return operand;
      } else {
        if (type == Token.INC || type == Token.DEC) {
          int operandType = operand.getType();
          if (operandType != Token.NAME && operandType != Token.GETPROP && operandType != Token.GETELEM) {
            String msg = (type == Token.INC) ? "invalid increment target" : "invalid decrement target";
            addError(msg);
          }
        }
        Node node = newNode(type, operand);
        if (exprNode.isPostfix()) {
          node.putBooleanProp(Node.INCRDECR_PROP, true);
        }
        return node;
      }
    }