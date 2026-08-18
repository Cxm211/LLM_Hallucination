public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
    if (coll == null) {
        throw new IllegalArgumentException("Collection must not be null");
    }
    if (coll instanceof UnmodifiableBoundedCollection) {
        return (BoundedCollection<E>) coll;
    }
    return new UnmodifiableBoundedCollection<E>(coll);
}