Node processUnaryExpression(UnaryExpression exprNode) {
      int type = transformTokenType(exprNode.getType());
      Node operand = transform(exprNode.getOperand());
      if (type == Token.NEG && operand.getType() == Token.NUMBER) {
        operand.setDouble(-operand.getDouble());
        return operand;
      } else {

        Node node = newNode(type, operand);
        if (exprNode.isPostfix()) {
          node.putBooleanProp(Node.INCRDECR_PROP, true);
        }
        // Validate increment/decrement targets
        if (type == Token.INC || type == Token.DEC) {
          int opType = operand.getType();
          boolean valid = (opType == Token.NAME || opType == Token.GETPROP || opType == Token.GETELEM);
          if (!valid) {
            if (type == Token.INC) {
              throw new RuntimeException("invalid increment target");
            } else {
              throw new RuntimeException("invalid decrement target");
            }
          }
        }
        return node;
      }
    }