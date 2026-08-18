// org/apache/commons/collections/TestCollectionUtils.java
public void testRemoveAllEmptyRemoveCollection() {
    List base = new ArrayList();
    base.add("A");
    base.add("B");
    List sub = new ArrayList();
    
    Collection result = CollectionUtils.removeAll(base, sub);
    assertEquals(2, result.size());
    assertEquals(true, result.contains("A"));
    assertEquals(true, result.contains("B"));
}