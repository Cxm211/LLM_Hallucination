// org/apache/commons/collections/list/TreeListTest.java
public void testBugCollections447_RemoveAtEnd() {
    final List<String> treeList = new TreeList<String>();
    treeList.add("A");
    treeList.add("B");
    treeList.add("C");
    
    final ListIterator<String> li = treeList.listIterator();
    assertEquals("A", li.next());
    assertEquals("B", li.next());
    assertEquals("C", li.next());
    
    li.remove(); // Deletes "C"
    
    // previous() after remove() should return "B"
    assertEquals("B", li.previous());
}