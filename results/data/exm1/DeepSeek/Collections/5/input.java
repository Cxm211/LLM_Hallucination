// buggy code
    public boolean addAll(int index, Collection coll) {
        // gets initial size
        final int sizeBefore = size();

        // adds all elements
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            add(it.next());
            // if it was inserted, then increase the target index
        }

        // compares sizes to detect if collection changed
        return sizeBefore != size();
    }

// relevant test
// org.apache.commons.collections.list.TestSetUniqueList::testListIteratorSet
    public void testListIteratorSet() {
        
        resetFull();
        ListIterator it = getList().listIterator();
        it.next();
        try {
            it.set(null);
            fail();
        } catch (UnsupportedOperationException ex) {}
    }

// org.apache.commons.collections.list.TestSetUniqueList::testListIteratorAdd
    public void testListIteratorAdd() {
        
        resetEmpty();
        List list1 = getList();
        List list2 = getConfirmedList();

        Object[] elements = getOtherElements();  
        ListIterator iter1 = list1.listIterator();
        ListIterator iter2 = list2.listIterator();

        for (int i = 0; i < elements.length; i++) {
            iter1.add(elements[i]);
            iter2.add(elements[i]);
            super.verify();  
        }

        resetFull();
        iter1 = getList().listIterator();
        iter2 = getConfirmedList().listIterator();
        for (int i = 0; i < elements.length; i++) {
            iter1.next();
            iter2.next();
            iter1.add(elements[i]);
            iter2.add(elements[i]);
            super.verify();  
        }
    }

// org.apache.commons.collections.list.TestSetUniqueList::testCollectionAddAll
    public void testCollectionAddAll() {
        
        resetEmpty();
        Object[] elements = getFullElements();
        boolean r = collection.addAll(Arrays.asList(elements));
        confirmed.addAll(Arrays.asList(elements));
        verify();
        assertTrue("Empty collection should change after addAll", r);
        for (int i = 0; i < elements.length; i++) {
            assertTrue("Collection should contain added element",
                       collection.contains(elements[i]));
        }

        resetFull();
        int size = collection.size();
        elements = getOtherElements();
        r = collection.addAll(Arrays.asList(elements));
        confirmed.addAll(Arrays.asList(elements));
        verify();
        assertTrue("Full collection should change after addAll", r);
        for (int i = 0; i < elements.length; i++) {
            assertTrue("Full collection should contain added element " + i,
                       collection.contains(elements[i]));
        }
        assertEquals("Size should increase after addAll", 
                     size + elements.length, collection.size());
    }

// org.apache.commons.collections.list.TestSetUniqueList::testIntCollectionAddAll
    public void testIntCollectionAddAll() {
      
      List list = new SetUniqueList(new ArrayList(), new HashSet());
      final Integer existingElement = new Integer(1);
      list.add(existingElement);

      
      final Integer firstNewElement = new Integer(2);
      final Integer secondNewElement = new Integer(3);
      collection = Arrays.asList(new Integer[] {firstNewElement, secondNewElement});
      list.addAll(0, collection);
      assertEquals("Unique elements should be added.", 3, list.size());
      assertEquals("First new element should be at index 0", firstNewElement, list.get(0));
      assertEquals("Second new element should be at index 1", secondNewElement, list.get(1));
      assertEquals("Existing element should shift to index 2", existingElement, list.get(2));

      
      final Integer thirdNewElement = new Integer(4);
      collection = Arrays.asList(new Integer[] {existingElement, thirdNewElement});
      list.addAll(0, collection);
      assertEquals("Duplicate element should not be added, unique element should be added.",
        4, list.size());
      assertEquals("Third new element should be at index 0", thirdNewElement, list.get(0));
    }

// org.apache.commons.collections.list.TestSetUniqueList::testListSetByIndex
    public void testListSetByIndex() {
        
        resetFull();
        int size = collection.size();
        getList().set(0, new Long(1000));
        assertEquals(size, collection.size());

        getList().set(2, new Long(1000));
        assertEquals(size - 1, collection.size());
        assertEquals(new Long(1000), getList().get(1));  
    }

// org.apache.commons.collections.list.TestSetUniqueList::testCollectionIteratorRemove
    public void testCollectionIteratorRemove() {
        try {
            extraVerify = false;
            super.testCollectionIteratorRemove();
        } finally {
            extraVerify = true;
        }
    }

// org.apache.commons.collections.list.TestSetUniqueList::testFactory
    public void testFactory() {
        Integer[] array = new Integer[] {new Integer(1), new Integer(2), new Integer(1)};
        ArrayList list = new ArrayList(Arrays.asList(array));
        final SetUniqueList lset = SetUniqueList.decorate(list);

        assertEquals("Duplicate element was added.", 2, lset.size());
        assertEquals(new Integer(1), lset.get(0));
        assertEquals(new Integer(2), lset.get(1));
        assertEquals(new Integer(1), list.get(0));
        assertEquals(new Integer(2), list.get(1));
    }

// org.apache.commons.collections.list.TestSetUniqueList::testAdd
    public void testAdd() {
        final SetUniqueList lset = new SetUniqueList(new ArrayList(), new HashSet());

        
        final Object obj = new Integer(1);
        lset.add(obj);
        lset.add(obj);
        assertEquals("Duplicate element was added.", 1, lset.size());

        
        lset.add(new Integer(2));
        assertEquals("Unique element was not added.", 2, lset.size());
    }

// org.apache.commons.collections.list.TestSetUniqueList::testAddAll
    public void testAddAll() {
        final SetUniqueList lset = new SetUniqueList(new ArrayList(), new HashSet());

        lset.addAll(
            Arrays.asList(new Integer[] { new Integer(1), new Integer(1)}));

        assertEquals("Duplicate element was added.", 1, lset.size());
    }

// org.apache.commons.collections.list.TestSetUniqueList::testSet
    public void testSet() {
        final SetUniqueList lset = new SetUniqueList(new ArrayList(), new HashSet());

        
        final Object obj1 = new Integer(1);
        final Object obj2 = new Integer(2);
        final Object obj3 = new Integer(3);

        lset.add(obj1);
        lset.add(obj2);
        lset.set(0, obj1);
        assertEquals(2, lset.size());
        assertSame(obj1, lset.get(0));
        assertSame(obj2, lset.get(1));

        lset.clear();
        lset.add(obj1);
        lset.add(obj2);
        lset.set(0, obj2);
        assertEquals(1, lset.size());
        assertSame(obj2, lset.get(0));

        lset.clear();
        lset.add(obj1);
        lset.add(obj2);
        lset.set(0, obj3);
        assertEquals(2, lset.size());
        assertSame(obj3, lset.get(0));
        assertSame(obj2, lset.get(1));

        lset.clear();
        lset.add(obj1);
        lset.add(obj2);
        lset.set(1, obj1);
        assertEquals(1, lset.size());
        assertSame(obj1, lset.get(0));
    }

// org.apache.commons.collections.list.TestSetUniqueList::testListIterator
    public void testListIterator() {
        final SetUniqueList lset = new SetUniqueList(new ArrayList(), new HashSet());

        final Object obj1 = new Integer(1);
        final Object obj2 = new Integer(2);
        lset.add(obj1);
        lset.add(obj2);

        
        for (final ListIterator it = lset.listIterator(); it.hasNext();) {
            it.next();

            if (!it.hasNext()) {
                it.add(obj1);
                break;
            }
        }

        assertEquals("Duplicate element was added", 2, lset.size());
    }

// org.apache.commons.collections.list.TestSetUniqueList::testUniqueListReInsert
    public void testUniqueListReInsert() {
        List l = SetUniqueList.decorate(new LinkedList());
        l.add(new Object());
        l.add(new Object());
        
        Object a = l.get(0);
        
        
        l.set(0, l.get(1)); 
        assertEquals(1, l.size());
        
        
        l.add(1, a); 
        assertEquals(2, l.size());
    }

// org.apache.commons.collections.list.TestSetUniqueList::testUniqueListDoubleInsert
    public void testUniqueListDoubleInsert() {
        List l = SetUniqueList.decorate(new LinkedList());
        l.add(new Object());
        l.add(new Object());
        
        
        l.set(0, l.get(1)); 
        assertEquals(1, l.size());
        
        
        l.add(1, l.get(0));
        assertEquals(1, l.size());
    }

// org.apache.commons.collections.list.TestSetUniqueList::testSetDownwardsInList
    public void testSetDownwardsInList() {
        
        ArrayList l = new ArrayList();
        HashSet s = new HashSet();
        final SetUniqueList ul = new SetUniqueList(l, s);

        Object a = new Object();
        Object b = new Object();
        ul.add(a);
        ul.add(b);
        assertEquals(a, l.get(0));
        assertEquals(b, l.get(1));
        assertTrue(s.contains(a)); 
        assertTrue(s.contains(b));
        
        assertEquals(a, ul.set(0, b));
        assertEquals(1, s.size());
        assertEquals(1, l.size());
        assertEquals(b, l.get(0));
        assertTrue(s.contains(b));
        assertFalse(s.contains(a));
    }

// org.apache.commons.collections.list.TestSetUniqueList::testSetInBiggerList
    public void testSetInBiggerList() {
        
        ArrayList l = new ArrayList();
        HashSet s = new HashSet();
        final SetUniqueList ul = new SetUniqueList(l, s);

        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        ul.add(a);
        ul.add(b);
        ul.add(c);
        assertEquals(a, l.get(0));
        assertEquals(b, l.get(1));
        assertEquals(c, l.get(2));
        assertTrue(s.contains(a)); 
        assertTrue(s.contains(b));
        assertTrue(s.contains(c));
        
        assertEquals(a, ul.set(0, b));
        assertEquals(2, s.size());
        assertEquals(2, l.size());
        assertEquals(b, l.get(0));
        assertEquals(c, l.get(1));
        assertFalse(s.contains(a));
        assertTrue(s.contains(b));
        assertTrue(s.contains(c));
    }

// org.apache.commons.collections.list.TestSetUniqueList::testSetUpwardsInList
    public void testSetUpwardsInList() {
        
        ArrayList l = new ArrayList();
        HashSet s = new HashSet();
        final SetUniqueList ul = new SetUniqueList(l, s);

        Object a = new String("A");
        Object b = new String("B");
        Object c = new String("C");

        ul.add(a);
        ul.add(b);
        ul.add(c);
        assertEquals(a, l.get(0));
        assertEquals(b, l.get(1));
        assertEquals(c, l.get(2));
        assertTrue(s.contains(a)); 
        assertTrue(s.contains(b));
        assertTrue(s.contains(c));
        
        assertEquals(b, ul.set(1, a));
        assertEquals(2, s.size());
        assertEquals(2, l.size());
        assertEquals(a, l.get(0));
        assertEquals(c, l.get(1));
        assertTrue(s.contains(a));
        assertFalse(s.contains(b));
        assertTrue(s.contains(c));
    }
