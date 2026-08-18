public static WildcardType subtypeOf(Type bound) {
    if (bound instanceof WildcardType) {
        WildcardType wildcard = (WildcardType) bound;
        Type[] lowerBounds = wildcard.getLowerBounds();
        if (lowerBounds.length == 0) {
            return wildcard;
        } else {
            return new WildcardTypeImpl(new Type[]{Object.class}, EMPTY_TYPE_ARRAY);
        }
    }
    return new WildcardTypeImpl(new Type[]{bound}, EMPTY_TYPE_ARRAY);
}