// org/apache/commons/collections/TestCollectionUtils.java
public void testRemoveAllWithDuplicates() {
        List base = new ArrayList();
        base.add("A");
        base.add("A");
        base.add("B");
        List sub = new ArrayList();
        sub.add("A");
        
        Collection result = CollectionUtils.removeAll(base, sub);
        assertEquals(1, result.size());
        assertEquals(true, result.contains("B"));
        assertEquals(false, result.contains("A"));
        assertEquals(3, base.size());
        assertEquals(1, sub.size());
    }
