public static WildcardType subtypeOf(Type bound) {
    if (bound instanceof WildcardType) {
      return new WildcardTypeImpl(((WildcardType) bound).getUpperBounds(), EMPTY_TYPE_ARRAY);
    }
    Type[] upperBounds = new Type[] { bound == null ? Object.class : bound };
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }