public static WildcardType supertypeOf(Type bound) {
    if (bound instanceof WildcardType) {
      WildcardType wildcard = (WildcardType) bound;
      Type[] lowerBounds = wildcard.getLowerBounds();
      return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
    }
    Type[] lowerBounds = new Type[] { bound };
    return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
}