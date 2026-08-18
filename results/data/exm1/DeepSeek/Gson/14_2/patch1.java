public static WildcardType supertypeOf(Type bound) {
    if (bound instanceof WildcardType) {
        WildcardType wildcard = (WildcardType) bound;
        Type[] lowerBounds = wildcard.getLowerBounds();
        if (lowerBounds.length == 1) {
            return wildcard;
        } else {
            return new WildcardTypeImpl(new Type[]{Object.class}, EMPTY_TYPE_ARRAY);
        }
    }
    return new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{bound});
}