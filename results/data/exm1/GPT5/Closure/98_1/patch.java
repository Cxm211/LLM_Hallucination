boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }

  // Make sure this assignment is not in a loop.
  return !ref.isInLoop();
}

public String getSourceName() {
  return sourceName;
}

BasicBlock(BasicBlock parent, Node root) {
  this.parent = parent;

  // only named functions may be hoisted.
  this.isHoisted = NodeUtil.isHoistedFunctionDeclaration(root);


}