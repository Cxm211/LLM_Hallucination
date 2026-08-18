// org/apache/commons/collections/list/TestSetUniqueList.java
public void testIntCollectionAddAllAtEnd() {
    // make a SetUniqueList with two elements
    List list = new SetUniqueList(new ArrayList(), new HashSet());
    final Integer firstElement = new Integer(1);
    final Integer secondElement = new Integer(2);
    list.add(firstElement);
    list.add(secondElement);

    // add two new unique elements at the end (index 2)
    final Integer thirdElement = new Integer(3);
    final Integer fourthElement = new Integer(4);
    Collection collection = Arrays.asList(new Integer[] {thirdElement, fourthElement});
    list.addAll(2, collection);
    assertEquals("Unique elements should be added at end.", 4, list.size());
    assertEquals("First element should remain at index 0", firstElement, list.get(0));
    assertEquals("Second element should remain at index 1", secondElement, list.get(1));
    assertEquals("Third element should be at index 2", thirdElement, list.get(2));
    assertEquals("Fourth element should be at index 3", fourthElement, list.get(3));
}