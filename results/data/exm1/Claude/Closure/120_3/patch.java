boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }

  BasicBlock block = ref.getBasicBlock();
  if (block == null) {
    return false;
  }

  for (; block != null; block = block.getParent()) {
    if (block.isFunction) {
      break;
    } else if (block.isLoop) {
      return false;
    }
  }

  return true;
}