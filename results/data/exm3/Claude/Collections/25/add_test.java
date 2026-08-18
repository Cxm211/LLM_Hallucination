// org/apache/commons/collections4/IteratorUtilsTest.java
@Test(expected = NullPointerException.class)
public void testCollatedIteratorVarArgsWithNullArray() {
    IteratorUtils.collatedIterator(null, (Iterator<Integer>[]) null);
}

@Test(expected = NullPointerException.class)
public void testCollatedIteratorVarArgsWithNullElement() {
    IteratorUtils.collatedIterator(null, collectionOdd.iterator(), null, collectionEven.iterator());
}

@Test(expected = NullPointerException.class)
public void testCollatedIteratorCollectionWithNullCollection() {
    IteratorUtils.collatedIterator(null, (Collection<Iterator<Integer>>) null);
}

@Test(expected = NullPointerException.class)
public void testCollatedIteratorCollectionWithNullElement() {
    Collection<Iterator<Integer>> iterators = new ArrayList<Iterator<Integer>>();
    iterators.add(collectionOdd.iterator());
    iterators.add(null);
    iterators.add(collectionEven.iterator());
    IteratorUtils.collatedIterator(null, iterators);
}