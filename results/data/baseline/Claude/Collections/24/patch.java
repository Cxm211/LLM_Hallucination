public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
        if (coll == null) {
            throw new NullPointerException("Collection must not be null");
        }
        @SuppressWarnings("unchecked")
        final BoundedCollection<E> result = (BoundedCollection<E>) new UnmodifiableBoundedCollection<>(coll);
        return result;
    }