BasicBlock(BasicBlock parent, Node root) {
  this.parent = parent;

  // only named functions may be hoisted.
  this.isHoisted = NodeUtil.isHoistedFunctionDeclaration(root);

  // Check if this block is inside a loop
  BasicBlock current = parent;
  boolean inLoop = false;
  while (current != null) {
    if (current.isLoop) {
      inLoop = true;
      break;
    }
    current = current.parent;
  }
  this.isInLoop = inLoop;

  // Track loop blocks
  if (root != null && isLoopStructure(root)) {
    this.isLoop = true;
  }
}