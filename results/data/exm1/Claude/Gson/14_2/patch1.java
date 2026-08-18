public static WildcardType supertypeOf(Type bound) {
    Type[] lowerBounds;
    if (bound instanceof WildcardType) {
      WildcardType wildcard = (WildcardType) bound;
      if (wildcard.getLowerBounds().length != 0) {
        lowerBounds = wildcard.getLowerBounds();
      } else {
        lowerBounds = EMPTY_TYPE_ARRAY;
      }
      return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
    } else {
      lowerBounds = new Type[] { bound };
      return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
    }
  }