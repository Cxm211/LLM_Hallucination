// org/apache/commons/collections/TestCollectionUtils.java
public void testRemoveAllWithDuplicates() {
    List base = new ArrayList();
    base.add("A");
    base.add("B");
    base.add("A");
    base.add("C");
    List sub = new ArrayList();
    sub.add("A");
    
    Collection result = CollectionUtils.removeAll(base, sub);
    assertEquals(2, result.size());
    assertEquals(false, result.contains("A"));
    assertEquals(true, result.contains("B"));
    assertEquals(true, result.contains("C"));
}