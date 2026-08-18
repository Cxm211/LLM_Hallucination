Node processAssignment(Assignment assignmentNode) {
  Node assign = processInfixExpression(assignmentNode);
  Node lhs = assign.getFirstChild();
  if (lhs != null && (lhs.isGetProp() || lhs.isGetElem())) {
    assign.putBooleanProp(Node.SIDE_EFFECT_FLAGS, true);
  }
  return assign;
}