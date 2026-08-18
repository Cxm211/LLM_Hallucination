public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Collection<Iterator<? extends E>> iterators) {
        if (iterators == null) {
            throw new NullPointerException("Iterators must not be null");
        }
        for (Iterator<? extends E> it : iterators) {
            if (it == null) {
                throw new NullPointerException("Iterators must not be null");
            }
        }
        return new CollatingIterator<E>(comparator, iterators);
    }