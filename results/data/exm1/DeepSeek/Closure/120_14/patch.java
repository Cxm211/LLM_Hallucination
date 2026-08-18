boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }

  // Make sure this assignment is not in a loop.
  Node n = ref.getNode();
  while (n != null) {
    if (n.isFunction()) {
      break;
    }
    if (n.isLoop()) {
      return false;
    }
    n = n.getParent();
  }

  return true;
}