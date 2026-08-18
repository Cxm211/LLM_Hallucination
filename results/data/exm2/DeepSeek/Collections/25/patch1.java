    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E>... iterators) {
        if (iterators == null) {
            throw new NullPointerException("iterators");
        }
        for (int i = 0; i < iterators.length; i++) {
            if (iterators[i] == null) {
                throw new NullPointerException("iterators[" + i + "]");
            }
        }
        return new CollatingIterator<E>(comparator, iterators);
    }