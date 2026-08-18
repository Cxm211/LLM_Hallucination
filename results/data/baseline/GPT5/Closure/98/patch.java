boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }
  BasicBlock block = ref.getBasicBlock();
  if (block != null && block.isInLoop()) {
    return false;
  }
  return true;
}