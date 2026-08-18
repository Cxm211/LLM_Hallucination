public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                               final Iterator<? extends E> iterator1,
                                               final Iterator<? extends E> iterator2) {
    final Comparator<? super E> comp = comparator == null ? ComparatorUtils.<E>naturalComparator() : comparator;
    return new CollatingIterator<E>(comp, iterator1, iterator2);
}