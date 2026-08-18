Node processAssignment(Assignment assignmentNode) {
      Node assign = processInfixExpression(assignmentNode);
      Node lhs = assign.getFirstChild();
      if (lhs != null && lhs.getType() == Token.GETPROP) {
        assign.putBooleanProp(Node.SIDE_EFFECT_FLAGS, Node.SIDE_EFFECTS_ALL);
      }
      return assign;
    }