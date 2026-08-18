BasicBlock(BasicBlock parent, Node root) {
  this.parent = parent;
  this.isHoisted = NodeUtil.isHoistedFunctionDeclaration(root);
}