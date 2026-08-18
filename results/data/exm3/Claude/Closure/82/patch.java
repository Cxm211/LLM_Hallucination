public final boolean isEmptyType() {
  return isNoType() || isNoObjectType() || isNoResolvedType() ||
      (isFunctionType() && isSubtypeOf(getNativeType(JSTypeNative.LEAST_FUNCTION_TYPE)));
}