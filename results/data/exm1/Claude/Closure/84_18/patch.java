Node processAssignment(Assignment assignmentNode) {
  Node assign = processInfixExpression(assignmentNode);
  Node lhs = assign.getFirstChild();
  if (lhs.getType() == Token.CALL) {
    if (assignmentNode.getType() == Token.ASSIGN) {
      errorReporter.error("Invalid assignment target", 
        sourceName, 
        lhs.getLineno(), "", lhs.getCharno());
    } else {
      errorReporter.error("Invalid assignment target", 
        sourceName, 
        lhs.getLineno(), "", lhs.getCharno());
    }
  }
  return assign;
}