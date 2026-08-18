// org/apache/commons/collections/list/TestSetUniqueList.java
public void testIntCollectionAddAllMiddleWithDuplicates() {
    // make a SetUniqueList with three elements
    List list = new SetUniqueList(new ArrayList(), new HashSet());
    final Integer firstElement = new Integer(1);
    final Integer secondElement = new Integer(2);
    final Integer thirdElement = new Integer(3);
    list.add(firstElement);
    list.add(secondElement);
    list.add(thirdElement);

    // add at index 1: one duplicate and one new element
    final Integer newElement = new Integer(4);
    Collection collection = Arrays.asList(new Integer[] {firstElement, newElement, secondElement});
    list.addAll(1, collection);
    assertEquals("Only unique element should be added.", 4, list.size());
    assertEquals("First element should remain at index 0", firstElement, list.get(0));
    assertEquals("New element should be at index 1", newElement, list.get(1));
    assertEquals("Second element should be at index 2", secondElement, list.get(2));
    assertEquals("Third element should be at index 3", thirdElement, list.get(3));
}