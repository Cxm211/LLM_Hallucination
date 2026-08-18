// org/apache/commons/collections4/list/SetUniqueListTest.java
public void testSubListClearIsUnmodifiable() {
    resetFull();
    List<E> subList = getCollection().subList(0, 2);
    try {
        subList.clear();
        fail("subList should be unmodifiable");
    } catch (UnsupportedOperationException e) {
        // expected
    }
}