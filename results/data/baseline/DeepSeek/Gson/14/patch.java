public static WildcardType subtypeOf(Type bound) {
    if (bound instanceof WildcardType) {
      WildcardType wildcard = (WildcardType) bound;
      Type[] upperBounds = wildcard.getUpperBounds();
      return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
    }
    Type[] upperBounds = new Type[] { bound };
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
}