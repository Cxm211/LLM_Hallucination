Node processAssignment(Assignment assignmentNode) {
  Node left = assignmentNode.getLeft();
  if (left.getType() == Token.ARRAYLIT || left.getType() == Token.OBJECTLIT) {
    addError("destructuring assignment forbidden");
    return newNode(Token.ERROR);
  }
  Node assign = processInfixExpression(assignmentNode);
  return assign;
}