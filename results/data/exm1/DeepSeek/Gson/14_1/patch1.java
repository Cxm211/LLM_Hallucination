public static WildcardType supertypeOf(Type bound) {
    if (bound instanceof WildcardType) {
        return (WildcardType) bound;
    }
    Type[] lowerBounds = new Type[] { bound };
    return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
}