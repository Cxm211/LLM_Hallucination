// org/apache/commons/collections/list/TestSetUniqueList.java
public void testSetNewElementUpdatesSet() {
    SetUniqueList list = SetUniqueList.decorate(new java.util.ArrayList());
    list.add("A");
    list.add("B");
    list.set(0, "C");
    assertTrue(list.contains("C"));
    assertFalse(list.contains("A"));
    int size = list.size();
    list.add("C");
    assertEquals(size, list.size());
}
