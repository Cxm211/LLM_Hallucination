    Node processAssignment(Assignment assignmentNode) {
      Node assign = processInfixExpression(assignmentNode);
      return assign;
    }

    Node processUnaryExpression(UnaryExpression exprNode) {
      int type = transformTokenType(exprNode.getType());
      Node operand = transform(exprNode.getOperand());
      if (type == Token.NEG && operand.getType() == Token.NUMBER) {
        Node folded = Node.newNumber(-operand.getDouble());
        return folded;
      } else {
        Node node = newNode(type, operand);
        if (exprNode.isPostfix()) {
          node.putBooleanProp(Node.INCRDECR_PROP, true);
        }
        return node;
      }
    }