// buggy code
    public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
        return new UnmodifiableBoundedCollection<E>(coll);
    }

