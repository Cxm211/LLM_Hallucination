// org/apache/commons/collections/list/TestSetUniqueList.java
public void testSetWithDuplicateAtDifferentPosition() {
    List list = new LinkedList();
    SetUniqueList decoratedList = SetUniqueList.decorate(list);
    String s1 = "Apple";
    String s2 = "Banana";
    String s3 = "Cherry";

    decoratedList.add(s1);
    decoratedList.add(s2);
    decoratedList.add(s3);

    assertEquals(3, decoratedList.size());
    assertEquals(s2, decoratedList.get(1));

    // Set position 1 to s3, which already exists at position 2
    Object removed = decoratedList.set(1, s3);

    assertEquals(s2, removed);
    assertEquals(2, decoratedList.size());
    assertEquals(s1, decoratedList.get(0));
    assertEquals(s3, decoratedList.get(1));
    assertTrue(decoratedList.contains(s1));
    assertTrue(decoratedList.contains(s3));
    assertFalse(decoratedList.contains(s2));
}