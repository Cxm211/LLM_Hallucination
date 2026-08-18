Node processAssignment(Assignment assignmentNode) {
  Node assign = processInfixExpression(assignmentNode);
  Node lhs = assign.getFirstChild();
  if (lhs != null) {
    int lhsType = lhs.getType();
    if (lhsType == Token.ARRAYLIT || lhsType == Token.OBJECTLIT) {
      // Mark LHS as destructuring to allow proper error reporting downstream.
      lhs.putBooleanProp(Node.DESTRUCTURING_LHS, true);
    }
  }
  return assign;
}
