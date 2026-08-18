boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }

  // Make sure this assignment is not in a loop.
  for (BasicBlock block = ref.getBasicBlock();
       block != null; block = block.getParent()) {
    if (block.isLoop) {
      return false;
    }
    if (block.isFunction) {
      // If the assignment is in a function that is not the one where the
      // variable is defined, it may be executed multiple times.
      if (block.getFunctionNode() != ref.getVariable().getScope().getRootNode()) {
        return false;
      }
      break;
    }
  }

  return true;
}