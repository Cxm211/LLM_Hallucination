    public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
        if (coll == null) {
            throw new NullPointerException("coll must not be null");
        }
        return new UnmodifiableBoundedCollection<E>(coll);
    }