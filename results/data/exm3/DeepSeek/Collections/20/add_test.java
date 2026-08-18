// org/apache/commons/collections/list/TreeListTest.java
public void testRemoveAfterPreviousAtBeginning() {
    final List<String> treeList = new TreeList<String>();
    treeList.add("A");
    treeList.add("B");
    final ListIterator<String> li = treeList.listIterator();
    assertEquals("A", li.next());
    assertEquals("A", li.previous());
    li.remove(); // Removes "A"
    assertFalse(li.hasPrevious());
    assertEquals("B", li.next());
}
