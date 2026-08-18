Node processAssignment(Assignment assignmentNode) {
      Node assign = processInfixExpression(assignmentNode);
      // Validate that the left-hand side is a valid assignment target
      Node lhs = assign.getFirstChild();
      int lhsType = lhs.getType();
      if (!(lhsType == Token.NAME || lhsType == Token.GETPROP || lhsType == Token.GETELEM)) {
        throw new RuntimeException("invalid assignment target");
      }
      return assign;
    }