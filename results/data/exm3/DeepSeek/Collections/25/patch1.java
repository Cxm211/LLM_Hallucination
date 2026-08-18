    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E>... iterators) {
        if (iterators.length == 0) {
            return Collections.<E>emptyIterator();
        }
        return new CollatingIterator<E>(comparator, iterators);
    }