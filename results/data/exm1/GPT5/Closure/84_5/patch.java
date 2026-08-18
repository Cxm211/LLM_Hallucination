Node processAssignment(Assignment assignmentNode) {
  int type = transformTokenType(assignmentNode.getType());
  Node left = transform(assignmentNode.getLeft());
  Node right = transform(assignmentNode.getRight());
  Node assign = newNode(type, left, right);
  return assign;
}
