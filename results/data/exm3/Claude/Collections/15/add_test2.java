// org/apache/commons/collections/list/TestSetUniqueList.java
public void testSetWithSameElement() {
    List list = new LinkedList();
    SetUniqueList decoratedList = SetUniqueList.decorate(list);
    String s1 = "Apple";
    String s2 = "Banana";

    decoratedList.add(s1);
    decoratedList.add(s2);

    assertEquals(2, decoratedList.size());

    // Set position 1 to itself
    Object removed = decoratedList.set(1, s2);

    assertEquals(s2, removed);
    assertEquals(2, decoratedList.size());
    assertEquals(s1, decoratedList.get(0));
    assertEquals(s2, decoratedList.get(1));
    assertTrue(decoratedList.contains(s1));
    assertTrue(decoratedList.contains(s2));
}