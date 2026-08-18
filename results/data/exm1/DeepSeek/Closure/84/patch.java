Node processAssignment(Assignment assignmentNode) {
  if (assignmentNode.getType() != Token.ASSIGN) {
    return assignmentNode;
  }
  Node assign = processInfixExpression(assignmentNode);
  return assign;
}