// org/apache/commons/collections4/list/SetUniqueListTest.java
public void testSubListModificationOperations() {
        resetFull();
        List<E> subList = getCollection().subList(1, 3);
        // Test add
        try {
            subList.add(subList.get(0));
            fail("add should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // Test set
        try {
            subList.set(0, subList.get(0));
            fail("set should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // Test remove by index
        try {
            subList.remove(0);
            fail("remove by index should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // Test clear
        try {
            subList.clear();
            fail("clear should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // Test addAll
        try {
            subList.addAll(java.util.Collections.emptyList());
            fail("addAll should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // Test remove by object
        try {
            subList.remove(subList.get(0));
            fail("remove by object should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // Test iterator remove
        try {
            java.util.Iterator<E> it = subList.iterator();
            it.next();
            it.remove();
            fail("iterator.remove should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
