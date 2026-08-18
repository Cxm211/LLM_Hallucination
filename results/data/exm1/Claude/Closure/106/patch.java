boolean canCollapseUnannotatedChildNames() {
  if (isClassOrEnum) {
    return true;
  }
  
  if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
    return false;
  }

  return (type == Type.FUNCTION || aliasingGets == 0) &&
      (parent == null || parent.canCollapseUnannotatedChildNames());
}