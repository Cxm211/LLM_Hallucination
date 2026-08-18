// buggy code
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

// relevant test
// org.apache.commons.collections.list.TreeListTest::testAddMultiple
    public void testAddMultiple() {
        final List<E> l = makeObject();
        l.add((E) "hugo");
        l.add((E) "erna");
        l.add((E) "daniel");
        l.add((E) "andres");
        l.add((E) "harald");
        l.add(0, null);
        assertEquals(null, l.get(0));
        assertEquals("hugo", l.get(1));
        assertEquals("erna", l.get(2));
        assertEquals("daniel", l.get(3));
        assertEquals("andres", l.get(4));
        assertEquals("harald", l.get(5));
    }

// org.apache.commons.collections.list.TreeListTest::testRemove
    public void testRemove() {
        final List<E> l = makeObject();
        l.add((E) "hugo");
        l.add((E) "erna");
        l.add((E) "daniel");
        l.add((E) "andres");
        l.add((E) "harald");
        l.add(0, null);
        int i = 0;
        assertEquals(null, l.get(i++));
        assertEquals("hugo", l.get(i++));
        assertEquals("erna", l.get(i++));
        assertEquals("daniel", l.get(i++));
        assertEquals("andres", l.get(i++));
        assertEquals("harald", l.get(i++));

        l.remove(0);
        i = 0;
        assertEquals("hugo", l.get(i++));
        assertEquals("erna", l.get(i++));
        assertEquals("daniel", l.get(i++));
        assertEquals("andres", l.get(i++));
        assertEquals("harald", l.get(i++));

        i = 0;
        l.remove(1);
        assertEquals("hugo", l.get(i++));
        assertEquals("daniel", l.get(i++));
        assertEquals("andres", l.get(i++));
        assertEquals("harald", l.get(i++));

        i = 0;
        l.remove(2);
        assertEquals("hugo", l.get(i++));
        assertEquals("daniel", l.get(i++));
        assertEquals("harald", l.get(i++));
    }

// org.apache.commons.collections.list.TreeListTest::testInsertBefore
    public void testInsertBefore() {
        final List<E> l = makeObject();
        l.add((E) "erna");
        l.add(0, (E) "hugo");
        assertEquals("hugo", l.get(0));
        assertEquals("erna", l.get(1));
    }

// org.apache.commons.collections.list.TreeListTest::testIndexOf
    public void testIndexOf() {
        final List<E> l = makeObject();
        l.add((E) "0");
        l.add((E) "1");
        l.add((E) "2");
        l.add((E) "3");
        l.add((E) "4");
        l.add((E) "5");
        l.add((E) "6");
        assertEquals(0, l.indexOf("0"));
        assertEquals(1, l.indexOf("1"));
        assertEquals(2, l.indexOf("2"));
        assertEquals(3, l.indexOf("3"));
        assertEquals(4, l.indexOf("4"));
        assertEquals(5, l.indexOf("5"));
        assertEquals(6, l.indexOf("6"));

        l.set(1, (E) "0");
        assertEquals(0, l.indexOf("0"));

        l.set(3, (E) "3");
        assertEquals(3, l.indexOf("3"));
        l.set(2, (E) "3");
        assertEquals(2, l.indexOf("3"));
        l.set(1, (E) "3");
        assertEquals(1, l.indexOf("3"));
        l.set(0, (E) "3");
        assertEquals(0, l.indexOf("3"));
    }

// org.apache.commons.collections.list.TreeListTest::testBug35258
    public void testBug35258() {
        final Object objectToRemove = new Integer(3);

        final List<Integer> treelist = new TreeList<Integer>();
        treelist.add(new Integer(0));
        treelist.add(new Integer(1));
        treelist.add(new Integer(2));
        treelist.add(new Integer(3));
        treelist.add(new Integer(4));

        
        treelist.remove(objectToRemove);

        final ListIterator<Integer> li = treelist.listIterator();
        assertEquals(new Integer(0), li.next());
        assertEquals(new Integer(0), li.previous());
        assertEquals(new Integer(0), li.next());
        assertEquals(new Integer(1), li.next());
        
        assertEquals(new Integer(1), li.previous());
        assertEquals(new Integer(1), li.next());
        assertEquals(new Integer(2), li.next());
        assertEquals(new Integer(2), li.previous());
        assertEquals(new Integer(2), li.next());
        assertEquals(new Integer(4), li.next());
        assertEquals(new Integer(4), li.previous());
        assertEquals(new Integer(4), li.next());
        assertEquals(false, li.hasNext());
    }

// org.apache.commons.collections.list.TreeListTest::testBugCollections447
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
                
        li.remove(); 
                
        
        
        assertEquals("A", li.previous());
    }
