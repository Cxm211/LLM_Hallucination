public final boolean isEmptyType() {
  return isNoType() || isNoObjectType() || isNoResolvedType() || (this instanceof UnknownType);
}