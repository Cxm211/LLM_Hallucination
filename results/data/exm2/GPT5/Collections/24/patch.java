public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
    if (coll == null) {
        throw new IllegalArgumentException("Collection must not be null.");
    }
    if (coll instanceof Unmodifiable) {
        @SuppressWarnings("unchecked")
        final BoundedCollection<E> result = (BoundedCollection<E>) coll;
        return result;
    }
    return new UnmodifiableBoundedCollection<E>(coll);
}