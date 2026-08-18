public static WildcardType supertypeOf(Type bound) {
  Type[] lowerBounds;
  lowerBounds = new Type[] { bound };
  return new WildcardTypeImpl(EMPTY_TYPE_ARRAY, lowerBounds);
}