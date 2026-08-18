boolean canCollapseUnannotatedChildNames() {
  if (type == Type.OTHER || globalSets != 1) {
    return false;
  }

  
  
  

  if (isClassOrEnum) {
    return true;
  }
  return (type == Type.FUNCTION || aliasingGets == 0) &&
      (parent == null || parent.canCollapseUnannotatedChildNames());
}