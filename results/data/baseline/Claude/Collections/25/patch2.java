public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                               final Collection<Iterator<? extends E>> iterators) {
    @SuppressWarnings("unchecked")
    final Comparator<E> comp = (Comparator<E>) comparator;
    return new CollatingIterator<E>(comp, iterators);
}