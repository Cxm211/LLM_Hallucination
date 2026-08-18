public static WildcardType subtypeOf(Type bound) {
    Type[] upperBounds;
    if (bound instanceof WildcardType) {
      WildcardType wildcard = (WildcardType) bound;
      if (wildcard.getLowerBounds().length == 0) {
        upperBounds = wildcard.getUpperBounds();
      } else {
        upperBounds = new Type[] { Object.class };
      }
    } else {
      upperBounds = new Type[] { bound };
    }
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }