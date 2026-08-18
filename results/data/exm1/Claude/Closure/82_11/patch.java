public final boolean isEmptyType() {
  return isNoType() || isNoObjectType() || isNoResolvedType() || (this instanceof UnionType && ((UnionType) this).getAlternates().isEmpty());
}