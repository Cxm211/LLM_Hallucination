boolean canCollapseUnannotatedChildNames() {
  if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
    return false;
  }

  if (isClassOrEnum) {
    return true;
  }
  return (type == Type.FUNCTION && aliasingGets == 0) &&
      (parent == null || parent.canCollapseUnannotatedChildNames());
}