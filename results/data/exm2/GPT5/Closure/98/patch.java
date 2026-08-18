boolean isAssignedOnceInLifetime() {
  Reference ref = getOneAndOnlyAssignment();
  if (ref == null) {
    return false;
  }
  Node n = ref.getNode();
  if (NodeUtil.isWithinLoop(n)) {
    return false;
  }
  return true;
}