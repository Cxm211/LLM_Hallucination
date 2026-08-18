public final boolean isEmptyType() {
  return isNoType() || isNoObjectType() || isNoResolvedType() ||
      (registry != null && registry.getNativeType(JSTypeNative.VOID_TYPE).equals(this));
}