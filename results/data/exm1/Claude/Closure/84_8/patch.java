Node processAssignment(Assignment assignmentNode) {
  Node assign = processInfixExpression(assignmentNode);
  Node lhs = assign.getFirstChild();
  if (lhs != null) {
    lhs.putBooleanProp(Node.IS_ASSIGNMENT_LHS, true);
  }
  return assign;
}