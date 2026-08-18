boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }

  // Make sure this assignment is not in a loop.
  for (BasicBlock block = ref.getBasicBlock();
       block != null; block = block.getParent()) {
    if (block.isFunction) {
      break;
    } else if (block.isLoop) {
      return false;
    }
  }

  // If we exited the loop without finding a function block, return false
  return block != null;
}