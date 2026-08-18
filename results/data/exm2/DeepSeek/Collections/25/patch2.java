    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Collection<Iterator<? extends E>> iterators) {
        if (iterators == null) {
            throw new NullPointerException("iterators");
        }
        for (Iterator<? extends E> iterator : iterators) {
            if (iterator == null) {
                throw new NullPointerException("iterators contains null");
            }
        }
        return new CollatingIterator<E>(comparator, iterators);
    }