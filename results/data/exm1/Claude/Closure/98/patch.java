boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }

  BasicBlock basicBlock = ref.getBasicBlock();
  if (basicBlock != null && !basicBlock.isAssignedOnceInLifetime()) {
    return false;
  }

  return true;
}