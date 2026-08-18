// ===== FIXED org.apache.commons.collections4.IteratorUtils :: collatedIterator(Comparator, Collection) [lines 650-655] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-25-fixed/src/main/java/org/apache/commons/collections4/IteratorUtils.java =====
    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Collection<Iterator<? extends E>> iterators) {
        @SuppressWarnings("unchecked")
        final Comparator<E> comp = comparator == null ? ComparatorUtils.NATURAL_COMPARATOR : comparator;
        return new CollatingIterator<E>(comp, iterators);
    }

// ===== FIXED org.apache.commons.collections4.IteratorUtils :: collatedIterator(Comparator, Iterator, Iterator) [lines 602-608] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-25-fixed/src/main/java/org/apache/commons/collections4/IteratorUtils.java =====
    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E> iterator1,
                                                   final Iterator<? extends E> iterator2) {
        @SuppressWarnings("unchecked")
        final Comparator<E> comp = comparator == null ? ComparatorUtils.NATURAL_COMPARATOR : comparator;
        return new CollatingIterator<E>(comp, iterator1, iterator2);
    }

// ===== FIXED org.apache.commons.collections4.IteratorUtils :: collatedIterator(Comparator, Iterator...) [lines 626-631] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-25-fixed/src/main/java/org/apache/commons/collections4/IteratorUtils.java =====
    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E>... iterators) {
        @SuppressWarnings("unchecked")
        final Comparator<E> comp = comparator == null ? ComparatorUtils.NATURAL_COMPARATOR : comparator;
        return new CollatingIterator<E>(comp, iterators);
    }
