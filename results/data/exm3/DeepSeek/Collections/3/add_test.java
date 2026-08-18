// org/apache/commons/collections/TestCollectionUtils.java
public void testRemoveAllWithEmptySecondCollection() {
        List base = new ArrayList();
        base.add("A");
        base.add("B");
        base.add("C");
        List sub = new ArrayList();
        
        Collection result = CollectionUtils.removeAll(base, sub);
        assertEquals(3, result.size());
        assertEquals(true, result.contains("A"));
        assertEquals(true, result.contains("B"));
        assertEquals(true, result.contains("C"));
        assertEquals(3, base.size());
        assertEquals(0, sub.size());
    }
