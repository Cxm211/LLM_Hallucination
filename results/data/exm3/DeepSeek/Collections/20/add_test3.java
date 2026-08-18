// org/apache/commons/collections/list/TreeListTest.java
public void testRemoveAfterNextThenPrevious() {
    final List<String> treeList = new TreeList<String>();
    treeList.add("A");
    treeList.add("B");
    treeList.add("C");
    treeList.add("D");
    final ListIterator<String> li = treeList.listIterator();
    assertEquals("A", li.next());
    assertEquals("B", li.next());
    li.remove(); // Removes "B"
    assertEquals("A", li.previous());
}
