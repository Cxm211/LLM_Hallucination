public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<E> coll) {
    return new UnmodifiableBoundedCollection<E>(coll);
}