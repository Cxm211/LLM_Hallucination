// org/apache/commons/collections/TestCollectionUtils.java::testRemoveAllWithDuplicates
public void testRemoveAllWithDuplicates() {
        List base = new ArrayList();
        base.add("A");
        base.add("A");
        base.add("B");
        List sub = new ArrayList();
        sub.add("A");
        sub.add("X");

        Collection result = CollectionUtils.removeAll(base, sub);
        assertEquals(1, result.size());
        assertEquals(false, result.contains("A"));
        assertEquals(true, result.contains("B"));
        assertEquals(3, base.size());
        assertEquals(true, base.contains("A"));
        assertEquals(true, base.contains("B"));
        assertEquals(2, sub.size());
        assertEquals(true, sub.contains("A"));
        assertEquals(true, sub.contains("X"));
    }