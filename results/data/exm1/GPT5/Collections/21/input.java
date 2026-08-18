// buggy code
    public List<E> subList(final int fromIndex, final int toIndex) {
        final List<E> superSubList = super.subList(fromIndex, toIndex);
        final Set<E> subSet = createSetBasedOnList(set, superSubList);
        return new SetUniqueList<E>(superSubList, subSet);
    }

// relevant test
// org.apache.commons.collections4.list.SetUniqueListTest::testListIteratorSet
    public void testListIteratorSet() {
        
        resetFull();
        final ListIterator<E> it = getCollection().listIterator();
        it.next();
        try {
            it.set(null);
            fail();
        } catch (final UnsupportedOperationException ex) {}
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testListIteratorAdd
    public void testListIteratorAdd() {
        
        resetEmpty();
        final List<E> list1 = getCollection();
        final List<E> list2 = getConfirmed();

        final E[] elements = getOtherElements();  
        ListIterator<E> iter1 = list1.listIterator();
        ListIterator<E> iter2 = list2.listIterator();

        for (final E element : elements) {
            iter1.add(element);
            iter2.add(element);
            super.verify();  
        }

        resetFull();
        iter1 = getCollection().listIterator();
        iter2 = getConfirmed().listIterator();
        for (final E element : elements) {
            iter1.next();
            iter2.next();
            iter1.add(element);
            iter2.add(element);
            super.verify();  
        }
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testCollectionAddAll
    public void testCollectionAddAll() {
        
        resetEmpty();
        E[] elements = getFullElements();
        boolean r = getCollection().addAll(Arrays.asList(elements));
        getConfirmed().addAll(Arrays.asList(elements));
        verify();
        assertTrue("Empty collection should change after addAll", r);
        for (final E element : elements) {
            assertTrue("Collection should contain added element",
                    getCollection().contains(element));
        }

        resetFull();
        final int size = getCollection().size();
        elements = getOtherElements();
        r = getCollection().addAll(Arrays.asList(elements));
        getConfirmed().addAll(Arrays.asList(elements));
        verify();
        assertTrue("Full collection should change after addAll", r);
        for (int i = 0; i < elements.length; i++) {
            assertTrue("Full collection should contain added element " + i,
                    getCollection().contains(elements[i]));
        }
        assertEquals("Size should increase after addAll",
                size + elements.length, getCollection().size());
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testIntCollectionAddAll
    public void testIntCollectionAddAll() {
      
      final List<Integer> list = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());
      final Integer existingElement = new Integer(1);
      list.add(existingElement);

      
      final Integer firstNewElement = new Integer(2);
      final Integer secondNewElement = new Integer(3);
      Collection<Integer> collection = Arrays.asList(new Integer[] {firstNewElement, secondNewElement});
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

// org.apache.commons.collections4.list.SetUniqueListTest::testListSetByIndex
    public void testListSetByIndex() {
        
        resetFull();
        final int size = getCollection().size();
        getCollection().set(0, (E) new Long(1000));
        assertEquals(size, getCollection().size());

        getCollection().set(2, (E) new Long(1000));
        assertEquals(size - 1, getCollection().size());
        assertEquals(new Long(1000), getCollection().get(1));  
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testCollectionIteratorRemove
    public void testCollectionIteratorRemove() {
        try {
            extraVerify = false;
            super.testCollectionIteratorRemove();
        } finally {
            extraVerify = true;
        }
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testFactory
    public void testFactory() {
        final Integer[] array = new Integer[] { new Integer(1), new Integer(2), new Integer(1) };
        final ArrayList<Integer> list = new ArrayList<Integer>(Arrays.asList(array));
        final SetUniqueList<Integer> lset = SetUniqueList.setUniqueList(list);

        assertEquals("Duplicate element was added.", 2, lset.size());
        assertEquals(new Integer(1), lset.get(0));
        assertEquals(new Integer(2), lset.get(1));
        assertEquals(new Integer(1), list.get(0));
        assertEquals(new Integer(2), list.get(1));
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testAdd
    public void testAdd() {
        final SetUniqueList<E> lset = new SetUniqueList<E>(new ArrayList<E>(), new HashSet<E>());

        
        final E obj = (E) new Integer(1);
        lset.add(obj);
        lset.add(obj);
        assertEquals("Duplicate element was added.", 1, lset.size());

        
        lset.add((E) new Integer(2));
        assertEquals("Unique element was not added.", 2, lset.size());
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testAddAll
    public void testAddAll() {
        final SetUniqueList<E> lset = new SetUniqueList<E>(new ArrayList<E>(), new HashSet<E>());

        lset.addAll(
            Arrays.asList((E[]) new Integer[] { new Integer(1), new Integer(1)}));

        assertEquals("Duplicate element was added.", 1, lset.size());
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testSet
    public void testSet() {
        final SetUniqueList<E> lset = new SetUniqueList<E>(new ArrayList<E>(), new HashSet<E>());

        
        final E obj1 = (E) new Integer(1);
        final E obj2 = (E) new Integer(2);
        final E obj3 = (E) new Integer(3);

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

// org.apache.commons.collections4.list.SetUniqueListTest::testListIterator
    public void testListIterator() {
        final SetUniqueList<E> lset = new SetUniqueList<E>(new ArrayList<E>(), new HashSet<E>());

        final E obj1 = (E) new Integer(1);
        final E obj2 = (E) new Integer(2);
        lset.add(obj1);
        lset.add(obj2);

        
        for (final ListIterator<E> it = lset.listIterator(); it.hasNext();) {
            it.next();

            if (!it.hasNext()) {
                it.add(obj1);
                break;
            }
        }

        assertEquals("Duplicate element was added", 2, lset.size());
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testUniqueListReInsert
    public void testUniqueListReInsert() {
        final List<E> l = SetUniqueList.setUniqueList(new LinkedList<E>());
        l.add((E) new Object());
        l.add((E) new Object());

        final E a = l.get(0);

        
        l.set(0, l.get(1));
        assertEquals(1, l.size());

        
        l.add(1, a);
        assertEquals(2, l.size());
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testUniqueListDoubleInsert
    public void testUniqueListDoubleInsert() {
        final List<E> l = SetUniqueList.setUniqueList(new LinkedList<E>());
        l.add((E) new Object());
        l.add((E) new Object());

        
        l.set(0, l.get(1));
        assertEquals(1, l.size());

        
        l.add(1, l.get(0));
        assertEquals(1, l.size());
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testSetDownwardsInList
    public void testSetDownwardsInList() {
        
        final ArrayList<E> l = new ArrayList<E>();
        final HashSet<E> s = new HashSet<E>();
        final SetUniqueList<E> ul = new SetUniqueList<E>(l, s);

        final E a = (E) new Object();
        final E b = (E) new Object();
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

// org.apache.commons.collections4.list.SetUniqueListTest::testSetInBiggerList
    public void testSetInBiggerList() {
        
        final ArrayList<E> l = new ArrayList<E>();
        final HashSet<E> s = new HashSet<E>();
        final SetUniqueList<E> ul = new SetUniqueList<E>(l, s);

        final E a = (E) new Object();
        final E b = (E) new Object();
        final E c = (E) new Object();

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

// org.apache.commons.collections4.list.SetUniqueListTest::testSetUpwardsInList
    public void testSetUpwardsInList() {
        
        final ArrayList<E> l = new ArrayList<E>();
        final HashSet<E> s = new HashSet<E>();
        final SetUniqueList<E> ul = new SetUniqueList<E>(l, s);

        final E a = (E) new String("A");
        final E b = (E) new String("B");
        final E c = (E) new String("C");

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

// org.apache.commons.collections4.list.SetUniqueListTest::testCollections304
    public void testCollections304() {
        final List<String> list = new LinkedList<String>();
        final SetUniqueList<String> decoratedList = SetUniqueList.setUniqueList(list);
        final String s1 = "Apple";
        final String s2 = "Lemon";
        final String s3 = "Orange";
        final String s4 = "Strawberry";

        decoratedList.add(s1);
        decoratedList.add(s2);
        decoratedList.add(s3);
        assertEquals(3, decoratedList.size());

        decoratedList.set(1, s4);
        assertEquals(3, decoratedList.size());

        decoratedList.add(1, s4);
        assertEquals(3, decoratedList.size());

        decoratedList.add(1, s2);
        assertEquals(4, decoratedList.size());
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testSubListIsUnmodifiable
    public void testSubListIsUnmodifiable() {
        resetFull();
        List<E> subList = getCollection().subList(1, 3);
        try {
            subList.remove(0);
            fail("subList should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            
        }
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testCollections307
    public void testCollections307() {
        List<E> list = new ArrayList<E>();
        List<E> uniqueList = SetUniqueList.setUniqueList(list);

        final String hello = "Hello";
        final String world = "World";
        uniqueList.add((E) hello);
        uniqueList.add((E) world);

        List<E> subList = list.subList(0, 0);
        List<E> subUniqueList = uniqueList.subList(0, 0);

        assertFalse(subList.contains(world)); 
        assertFalse(subUniqueList.contains(world)); 

        List<E> worldList = new ArrayList<E>();
        worldList.add((E) world);
        assertFalse(subList.contains("World")); 
        assertFalse(subUniqueList.contains("World")); 

        
        
        list = new ArrayList<E>();
        uniqueList = new SetUniqueList307(list, new java.util.TreeSet<E>());

        uniqueList.add((E) hello);
        uniqueList.add((E) world);

        subList = list.subList(0, 0);
        subUniqueList = uniqueList.subList(0, 0);

        assertFalse(subList.contains(world)); 
        assertFalse(subUniqueList.contains(world)); 

        worldList = new ArrayList<E>();
        worldList.add((E) world);
        assertFalse(subList.contains("World")); 
        assertFalse(subUniqueList.contains("World")); 
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testRetainAll
    public void testRetainAll() {
        final List<E> list = new ArrayList<E>(10);
        final SetUniqueList<E> uniqueList = SetUniqueList.setUniqueList(list);
        for (int i = 0; i < 10; ++i) {
            uniqueList.add((E)Integer.valueOf(i));
        }
        
        final Collection<E> retained = new ArrayList<E>(5);
        for (int i = 0; i < 5; ++i) {
            retained.add((E)Integer.valueOf(i * 2));
        }
        
        assertTrue(uniqueList.retainAll(retained));
        assertEquals(5, uniqueList.size());
        assertTrue(uniqueList.contains(Integer.valueOf(0)));
        assertTrue(uniqueList.contains(Integer.valueOf(2)));
        assertTrue(uniqueList.contains(Integer.valueOf(4)));
        assertTrue(uniqueList.contains(Integer.valueOf(6)));
        assertTrue(uniqueList.contains(Integer.valueOf(8)));
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testRetainAllWithInitialList
    public void testRetainAllWithInitialList() {
        
        final List<E> list = new ArrayList<E>(10);
        for (int i = 0; i < 5; ++i) {
            list.add((E)Integer.valueOf(i));
        }
        final SetUniqueList<E> uniqueList = SetUniqueList.setUniqueList(list);
        for (int i = 5; i < 10; ++i) {
            uniqueList.add((E)Integer.valueOf(i));
        }
        
        final Collection<E> retained = new ArrayList<E>(5);
        for (int i = 0; i < 5; ++i) {
            retained.add((E)Integer.valueOf(i * 2));
        }
        
        assertTrue(uniqueList.retainAll(retained));
        assertEquals(5, uniqueList.size());
        assertTrue(uniqueList.contains(Integer.valueOf(0)));
        assertTrue(uniqueList.contains(Integer.valueOf(2)));
        assertTrue(uniqueList.contains(Integer.valueOf(4)));
        assertTrue(uniqueList.contains(Integer.valueOf(6)));
        assertTrue(uniqueList.contains(Integer.valueOf(8)));
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testRetainAllCollections427
    public void testRetainAllCollections427() {
        final int size = 50000;
        final ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        final SetUniqueList<Integer> uniqueList = SetUniqueList.setUniqueList(list);
        final ArrayList<Integer> toRetain = new ArrayList<Integer>();
        for (int i = size; i < 2*size; i++) {
            toRetain.add(i);
        }

        final long start = System.currentTimeMillis();
        uniqueList.retainAll(toRetain);
        final long stop = System.currentTimeMillis();
        
        
        
        
        assertTrue(stop - start < 5000);
    }

// org.apache.commons.collections4.list.SetUniqueListTest::testSetCollections444
    public void testSetCollections444() {
        final SetUniqueList<Integer> lset = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());

        
        final Integer obj1 = new Integer(1);
        final Integer obj2 = new Integer(2);

        lset.add(obj1);
        lset.add(obj2);
        lset.set(0, obj1);
        assertEquals(2, lset.size());
        assertSame(obj1, lset.get(0));
        assertSame(obj2, lset.get(1));

        assertTrue(lset.contains(obj1));
        assertTrue(lset.contains(obj2));
    }
