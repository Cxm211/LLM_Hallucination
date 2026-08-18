// buggy function
    public List<E> subList(final int fromIndex, final int toIndex) {
        final List<E> superSubList = super.subList(fromIndex, toIndex);
        final Set<E> subSet = createSetBasedOnList(set, superSubList);
        return new SetUniqueList<E>(superSubList, subSet);
    }

// trigger testcase
// org/apache/commons/collections4/list/SetUniqueListTest.java::testSubListIsUnmodifiable
public void testSubListIsUnmodifiable() {
        resetFull();
        List<E> subList = getCollection().subList(1, 3);
        try {
            subList.remove(0);
            fail("subList should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
