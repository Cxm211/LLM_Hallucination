// org/apache/commons/collections4/list/SetUniqueListTest.java
public void testSubListAddIsUnmodifiable() {
    resetFull();
    List<E> subList = getCollection().subList(1, 3);
    try {
        subList.add((E) "test");
        fail("subList should be unmodifiable");
    } catch (UnsupportedOperationException e) {
        // expected
    }
}