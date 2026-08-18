  public static WildcardType subtypeOf(Type bound) {
    if (bound instanceof WildcardType) {
        WildcardType wildcard = (WildcardType) bound;
        Type[] upperBounds = wildcard.getUpperBounds();
        Type[] lowerBounds = wildcard.getLowerBounds();
        if (lowerBounds.length == 0) {
            // ? extends Something
            return subtypeOf(upperBounds[0]);
        } else {
            // ? super Something -> ? extends Object
            return new WildcardTypeImpl(new Type[] { Object.class }, EMPTY_TYPE_ARRAY);
        }
    }
    return new WildcardTypeImpl(new Type[] { canonicalize(bound) }, EMPTY_TYPE_ARRAY);
  }