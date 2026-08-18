// org/apache/commons/collections4/list/SetUniqueListTest.java::testSubListIsUnmodifiable
public void testSubListClearIsUnmodifiable() {
        resetFull();
        List<E> subList = getCollection().subList(1, 3);
        try {
            subList.clear();
            fail("subList should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }