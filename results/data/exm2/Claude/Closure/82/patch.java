public final boolean isEmptyType() {
  return isNoType() || isNoObjectType() || isNoResolvedType() ||
      (isFunctionType() && (isLeastFunctionType() || isGreatestFunctionType()));
}