public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
        if (coll == null) {
            throw new IllegalArgumentException("Collection must not be null.");
        }
        if (coll instanceof Unmodifiable) {
            @SuppressWarnings("unchecked")
            final BoundedCollection<E> result = (BoundedCollection<E>) coll;
            return result;
        }
        @SuppressWarnings("unchecked")
        final BoundedCollection<E> cast = (BoundedCollection<E>) coll;
        return new UnmodifiableBoundedCollection<E>(cast);
    }