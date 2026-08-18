// org/apache/commons/collections/list/TreeListTest.java
public void testRemoveAfterPreviousAtEnd() {
    final List<String> treeList = new TreeList<String>();
    treeList.add("A");
    treeList.add("B");
    treeList.add("C");
    final ListIterator<String> li = treeList.listIterator();
    while (li.hasNext()) {
        li.next();
    }
    assertEquals("C", li.previous());
    li.remove(); // Removes "C"
    assertEquals("B", li.previous());
}
