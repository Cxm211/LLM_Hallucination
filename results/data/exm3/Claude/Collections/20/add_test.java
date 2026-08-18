// org/apache/commons/collections/list/TreeListTest.java
public void testBugCollections447_RemoveAfterNext() {
    final List<String> treeList = new TreeList<String>();
    treeList.add("A");
    treeList.add("B");
    treeList.add("C");
    treeList.add("D");
    
    final ListIterator<String> li = treeList.listIterator();
    assertEquals("A", li.next());
    assertEquals("B", li.next());
    
    li.remove(); // Deletes "B"
    
    // next() after remove() should return the element after the one just removed
    assertEquals("C", li.next());
}