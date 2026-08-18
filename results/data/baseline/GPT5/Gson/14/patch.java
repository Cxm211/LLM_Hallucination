public static WildcardType subtypeOf(Type bound) {
  if (bound instanceof WildcardType) {
    return (WildcardType) bound;
  }
  Type[] upperBounds = new Type[] { bound };
  return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
}