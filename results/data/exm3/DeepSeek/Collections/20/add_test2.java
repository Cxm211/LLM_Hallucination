// org/apache/commons/collections/list/TreeListTest.java
public void testRemoveAfterPreviousSingleElement() {
    final List<String> treeList = new TreeList<String>();
    treeList.add("A");
    final ListIterator<String> li = treeList.listIterator();
    assertEquals("A", li.next());
    assertEquals("A", li.previous());
    li.remove(); // Removes "A", list empty
    assertFalse(li.hasPrevious());
    assertFalse(li.hasNext());
}
