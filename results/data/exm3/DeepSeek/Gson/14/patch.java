  public static WildcardType subtypeOf(Type bound) {
    if (bound instanceof WildcardType) {
      WildcardType w = (WildcardType) bound;
      Type[] upperBounds = w.getUpperBounds();
      return subtypeOf(upperBounds[0]);
    }
    Type[] upperBounds = new Type[] { bound };
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }