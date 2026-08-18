boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }

  // Make sure this assignment is not in a loop.
  // Only break when we reach the function that defines the variable.
  // Otherwise, continue to check outer loops.
  BasicBlock functionBlock = null;
  for (BasicBlock block = ref.getBasicBlock();
       block != null; block = block.getParent()) {
    if (block.isFunction) {
      functionBlock = block;
      break;
    } else if (block.isLoop) {
      return false;
    }
  }

  // If we reached a function block and the variable is defined in that function,
  // then no loops in that function, so it's safe.
  // Otherwise, continue checking outer blocks for loops.
  if (functionBlock != null && 
      ref.getVariable().getScope().getRootNode() == functionBlock.getFunction()) {
    return true;
  }

  // Continue checking outer blocks for loops
  BasicBlock outerBlock = functionBlock.getParent();
  while (outerBlock != null) {
    if (outerBlock.isLoop) {
      return false;
    }
    outerBlock = outerBlock.getParent();
  }
  return true;
}