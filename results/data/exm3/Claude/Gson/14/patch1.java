public static WildcardType supertypeOf(Type bound) {
    Type[] lowerBounds;
    if (bound instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) bound;
      lowerBounds = wildcardType.getLowerBounds();
    } else {
      lowerBounds = new Type[] { bound };
    }
    return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
  }