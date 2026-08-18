    public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
        if (coll == null) {
            throw new IllegalArgumentException("Collection must not be null");
        }
        if (coll instanceof UnmodifiableBoundedCollection) {
            // safe to cast because UnmodifiableBoundedCollection is a decorator and the underlying type is compatible
            return (UnmodifiableBoundedCollection<E>) coll;
        }
        return new UnmodifiableBoundedCollection<E>(coll);
    }