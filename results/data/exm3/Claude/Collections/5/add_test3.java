// org/apache/commons/collections/list/TestSetUniqueList.java
public void testIntCollectionAddAllOnlyDuplicates() {
    // make a SetUniqueList with two elements
    List list = new SetUniqueList(new ArrayList(), new HashSet());
    final Integer firstElement = new Integer(1);
    final Integer secondElement = new Integer(2);
    list.add(firstElement);
    list.add(secondElement);

    // try to add only duplicates at index 1
    Collection collection = Arrays.asList(new Integer[] {firstElement, secondElement});
    boolean changed = list.addAll(1, collection);
    assertEquals("Size should remain unchanged.", 2, list.size());
    assertEquals("First element should remain at index 0", firstElement, list.get(0));
    assertEquals("Second element should remain at index 1", secondElement, list.get(1));
    assertFalse("addAll should return false when no elements added.", changed);
}