boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }
  BasicBlock block = ref.getBasicBlock();
  while (block != null) {
    if (block.isLoop) {
      return false;
    }
    block = block.parent;
  }
  return true;
}