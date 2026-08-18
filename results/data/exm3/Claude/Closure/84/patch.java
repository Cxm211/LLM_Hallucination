Node processAssignment(Assignment assignmentNode) {
  Node assign = processInfixExpression(assignmentNode);
  Node left = assign.getFirstChild();
  if (left != null && left.isArrayLit()) {
    errorReporter.error(
        "destructuring assignment forbidden",
        sourceName,
        left.getLineno(), "", 0);
  }
  return assign;
}