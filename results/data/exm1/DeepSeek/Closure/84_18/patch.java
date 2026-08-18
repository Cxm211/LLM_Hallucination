Node processAssignment(Assignment assignmentNode) {
      Node assign = processInfixExpression(assignmentNode);
      Node left = assign.getFirstChild();
      if (left != null) {
        int type = left.getType();
        if (type != Token.NAME && type != Token.GETPROP && type != Token.GETELEM) {
          addError("invalid assignment target");
        }
      }
      return assign;
    }