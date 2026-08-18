    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E> iterator1,
                                                   final Iterator<? extends E> iterator2) {
        if (iterator1 == null) {
            throw new NullPointerException("iterator1");
        }
        if (iterator2 == null) {
            throw new NullPointerException("iterator2");
        }
        return new CollatingIterator<E>(comparator, iterator1, iterator2);
    }