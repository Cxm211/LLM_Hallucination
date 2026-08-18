Node processAssignment(Assignment assignmentNode) {
  Node assign = processInfixExpression(assignmentNode);
  assign.copyInformationFrom(assignmentNode);
  return assign;
}