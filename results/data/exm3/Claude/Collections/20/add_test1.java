// org/apache/commons/collections/list/TreeListTest.java
public void testBugCollections447_RemoveAtStart() {
    final List<String> treeList = new TreeList<String>();
    treeList.add("A");
    treeList.add("B");
    treeList.add("C");
    
    final ListIterator<String> li = treeList.listIterator();
    assertEquals("A", li.next());
    assertEquals("B", li.next());
    assertEquals("A", li.previous());
    
    li.remove(); // Deletes "A"
    
    // next() after remove() should return "B"
    assertEquals("B", li.next());
}