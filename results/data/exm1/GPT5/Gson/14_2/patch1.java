public static WildcardType supertypeOf(Type bound) {
    if (bound instanceof WildcardType) {
      return new WildcardTypeImpl(new Type[] { Object.class }, ((WildcardType) bound).getLowerBounds());
    }
    Type[] lowerBounds = bound == null ? EMPTY_TYPE_ARRAY : new Type[] { bound };
    return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
  }