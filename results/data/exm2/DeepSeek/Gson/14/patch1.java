  public static WildcardType supertypeOf(Type bound) {
    if (bound instanceof WildcardType) {
        WildcardType wildcard = (WildcardType) bound;
        Type[] upperBounds = wildcard.getUpperBounds();
        Type[] lowerBounds = wildcard.getLowerBounds();
        if (lowerBounds.length == 0) {
            // ? extends Something -> ? extends Object
            return subtypeOf(Object.class);
        } else {
            // ? super Something
            return supertypeOf(lowerBounds[0]);
        }
    }
    return new WildcardTypeImpl(new Type[] { Object.class }, new Type[] { canonicalize(bound) });
  }