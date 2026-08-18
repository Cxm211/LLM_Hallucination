Node processAssignment(Assignment assignmentNode) {
  Node assign = processInfixExpression(assignmentNode);
  Node left = assign.getFirstChild();
  if (left != null && !isValidAssignmentTarget(left)) {
    errorReporter.error("invalid assignment target", sourceName, left.getLineno(), "", 0);
  }
  return assign;
}