Node processUnaryExpression(UnaryExpression exprNode) {
      int type = transformTokenType(exprNode.getType());
      Node operand = transform(exprNode.getOperand());
      if (type == Token.NEG && operand.getType() == Token.NUMBER) {
        double value = operand.getDouble();
        // Preserve -0 semantics: don't fold -0 to 0
        if (value != 0.0) {
          return Node.newNumber(-value);
        }
        // For 0, keep as a NEG node to preserve sign
      }
      Node node = newNode(type, operand);
      if ((type == Token.INC || type == Token.DEC) && exprNode.isPostfix()) {
        node.putBooleanProp(Node.INCRDECR_PROP, true);
      }
      return node;
    }