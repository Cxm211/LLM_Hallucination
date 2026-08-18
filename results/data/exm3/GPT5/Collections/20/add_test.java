// org/apache/commons/collections/list/TreeListTest.java::testRemoveAfterPreviousThenNext
public void testRemoveAfterPreviousThenNext() {
        final List<String> treeList = new TreeList<String>();
        treeList.add("A");
        treeList.add("B");
        treeList.add("C");
        treeList.add("D");

        final ListIterator<String> li = treeList.listIterator();
        assertEquals("A", li.next());
        assertEquals("B", li.next());

        assertEquals("B", li.previous());
        li.remove(); // Deletes "B"

        // next() after remove() should move to the element after the one just removed
        assertEquals("C", li.next());
    }