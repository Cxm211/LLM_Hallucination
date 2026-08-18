  public static WildcardType supertypeOf(Type bound) {
    if (bound instanceof WildcardType) {
      WildcardType w = (WildcardType) bound;
      Type[] lowerBounds = w.getLowerBounds();
      if (lowerBounds.length == 0) {
        return subtypeOf(Object.class);
      } else {
        return supertypeOf(lowerBounds[0]);
      }
    }
    Type[] lowerBounds = new Type[] { bound };
    return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
  }