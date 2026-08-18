public void onRedeclaration(
    Scope s, String name, Node n, Node parent, Node gramps,
    Node nodeWithLineNumber) {
  Preconditions.checkState(n.getType() == Token.NAME);
  if (parent.getType() == Token.VAR) {
  // If name is "arguments", Var maybe null.
    Preconditions.checkState(parent.hasOneChild());

    replaceVarWithAssignment(n, parent, gramps);
  } else if (parent.getType() == Token.FUNCTION) {
    // Handle function declarations - remove the function declaration node
    // but keep the function itself (it will be normalized elsewhere)
  }
}