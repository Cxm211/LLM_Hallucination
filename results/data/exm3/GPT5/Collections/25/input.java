// buggy function
    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E> iterator1,
                                                   final Iterator<? extends E> iterator2) {
        return new CollatingIterator<E>(comparator, iterator1, iterator2);
    }

    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E>... iterators) {
        return new CollatingIterator<E>(comparator, iterators);
    }

    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Collection<Iterator<? extends E>> iterators) {
        return new CollatingIterator<E>(comparator, iterators);
    }

// trigger testcase
// org/apache/commons/collections4/IteratorUtilsTest.java::testCollatedIterator
@Test
    public void testCollatedIterator() {
        try {
            IteratorUtils.collatedIterator(null, collectionOdd.iterator(), null);
            fail("expecting NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        try {
            IteratorUtils.collatedIterator(null, null, collectionEven.iterator());
            fail("expecting NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        // natural ordering
        Iterator<Integer> it = 
                IteratorUtils.collatedIterator(null, collectionOdd.iterator(), collectionEven.iterator());

        List<Integer> result = IteratorUtils.toList(it);
        assertEquals(12, result.size());

        List<Integer> combinedList = new ArrayList<Integer>();
        combinedList.addAll(collectionOdd);
        combinedList.addAll(collectionEven);
        Collections.sort(combinedList);

        assertEquals(combinedList, result);

        it = IteratorUtils.collatedIterator(null, collectionOdd.iterator(), emptyCollection.iterator());
        result = IteratorUtils.toList(it);
        assertEquals(collectionOdd, result);

        final Comparator<Integer> reverseComparator =
                ComparatorUtils.reversedComparator(ComparatorUtils.<Integer>naturalComparator());

        Collections.reverse((List<Integer>) collectionOdd);
        Collections.reverse((List<Integer>) collectionEven);
        Collections.reverse(combinedList);

        it = IteratorUtils.collatedIterator(reverseComparator,
                                            collectionOdd.iterator(),
                                            collectionEven.iterator());
        result = IteratorUtils.toList(it);
        assertEquals(combinedList, result);
    }
