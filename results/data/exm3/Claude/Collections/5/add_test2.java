// org/apache/commons/collections/list/TestSetUniqueList.java
public void testIntCollectionAddAllEmptyCollection() {
    // make a SetUniqueList with one element
    List list = new SetUniqueList(new ArrayList(), new HashSet());
    final Integer element = new Integer(1);
    list.add(element);

    // add empty collection at index 0
    Collection collection = new ArrayList();
    boolean changed = list.addAll(0, collection);
    assertEquals("Size should remain unchanged.", 1, list.size());
    assertEquals("Element should remain at index 0", element, list.get(0));
    assertFalse("addAll should return false for empty collection.", changed);
}