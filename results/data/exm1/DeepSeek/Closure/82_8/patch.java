public final boolean isEmptyType() {
    return isNoType() || isNoObjectType() || isNoResolvedType() || isNullType() || isVoidType();
  }