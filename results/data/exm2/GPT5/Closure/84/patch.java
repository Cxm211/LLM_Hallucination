    Node processAssignment(Assignment assignmentNode) {
      Node lhs = assignmentNode.getLeft();
      if (lhs != null) {
        int lhsType = lhs.getType();
        if (lhsType == Token.ARRAYLIT || lhsType == Token.OBJECTLIT) {
          throw new RuntimeException("destructuring assignment forbidden");
        }
      }
      Node assign = processInfixExpression(assignmentNode);
      return assign;
    }