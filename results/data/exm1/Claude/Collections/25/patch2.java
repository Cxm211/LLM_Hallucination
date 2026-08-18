public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                               final Collection<Iterator<? extends E>> iterators) {
    final Comparator<? super E> comp = comparator == null ? ComparatorUtils.<E>naturalComparator() : comparator;
    return new CollatingIterator<E>(comp, iterators);
}