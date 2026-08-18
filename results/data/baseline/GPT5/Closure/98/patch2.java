BasicBlock(BasicBlock parent, Node root) {
  this.parent = parent;
  this.isHoisted = NodeUtil.isHoistedFunctionDeclaration(root);
  this.isInLoop = (parent != null && parent.isInLoop()) || NodeUtil.isWithinLoop(root);
}