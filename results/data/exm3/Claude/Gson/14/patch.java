public static WildcardType subtypeOf(Type bound) {
    Type[] upperBounds;
    if (bound instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) bound;
      upperBounds = wildcardType.getUpperBounds();
    } else {
      upperBounds = new Type[] { bound };
    }
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }