    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Collection<Iterator<? extends E>> iterators) {
        if (iterators.isEmpty()) {
            return Collections.<E>emptyIterator();
        }
        return new CollatingIterator<E>(comparator, iterators);
    }