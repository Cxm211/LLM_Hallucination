    Node processAssignment(Assignment assignmentNode) {
      Node target = assignmentNode.getTarget();
      if (target.getType() == Token.ARRAYLIT || target.getType() == Token.OBJECTLIT) {
        addError("destructuring assignment forbidden");
        addError("invalid assignment target");
      }
      Node assign = processInfixExpression(assignmentNode);
      return assign;
    }