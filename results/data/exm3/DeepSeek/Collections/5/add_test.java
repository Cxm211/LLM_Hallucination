// org/apache/commons/collections/list/TestSetUniqueList.java
public void testAddAllAtIndexOneWithMixedElements() {
    List list = new SetUniqueList(new ArrayList(), new HashSet());
    final Integer existingElement = new Integer(1);
    list.add(existingElement);
    final Integer anotherElement = new Integer(2);
    list.add(anotherElement);
    final Integer new1 = new Integer(3);
    final Integer duplicate = existingElement;
    final Integer new2 = new Integer(4);
    Collection collection = Arrays.asList(new Integer[] {new1, duplicate, new2});
    list.addAll(1, collection);
    assertEquals("Size should be 4", 4, list.size());
    assertEquals("Element at index 0 should be 1", existingElement, list.get(0));
    assertEquals("Element at index 1 should be 3", new1, list.get(1));
    assertEquals("Element at index 2 should be 4", new2, list.get(2));
    assertEquals("Element at index 3 should be 2", anotherElement, list.get(3));
}
