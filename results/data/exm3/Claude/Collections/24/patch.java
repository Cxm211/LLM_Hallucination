public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
    if (coll == null) {
        throw new IllegalArgumentException("Collection must not be null");
    }
    if (coll instanceof UnmodifiableBoundedCollection) {
        @SuppressWarnings("unchecked")
        final BoundedCollection<E> tmpColl = (BoundedCollection<E>) coll;
        return tmpColl;
    }
    return new UnmodifiableBoundedCollection<E>(coll);
}