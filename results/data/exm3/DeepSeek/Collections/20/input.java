// buggy function
        public void remove() {
            checkModCount();
            if (currentIndex == -1) {
                throw new IllegalStateException();
            }
            if (nextIndex == currentIndex) {
                next = next.next();
                parent.remove(currentIndex);
            } else {
                // remove() following next()
                parent.remove(currentIndex);
                nextIndex--;
            }
            // the AVL node referenced by next may have become stale after a remove
            // reset it now: will be retrieved by next call to next()/previous() via nextIndex
            current = null;
            currentIndex = -1;
            expectedModCount++;
        }

// trigger testcase
// org/apache/commons/collections/list/TreeListTest.java::testBugCollections447
public void testBugCollections447() {
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
                
        // previous() after remove() should move to
        // the element before the one just removed
        assertEquals("A", li.previous());
    }
