// buggy code
    public boolean retainAll(Collection<?> coll) {
        boolean result = collection.retainAll(coll);
        if (result == false) {
            return false;
        }
        if (collection.size() == 0) {
            setOrder.clear();
        } else {
            for (Iterator<E> it = setOrder.iterator(); it.hasNext();) {
                if (!collection.contains(it.next())) {
                    it.remove();
                }
            }
        }
        return result;
    }

// relevant test
// org.apache.commons.collections.set.ListOrderedSet2Test::testOrdering
    public void testOrdering() {
        ListOrderedSet<E> set = setupSet();
        Iterator<E> it = set.iterator();

        for (int i = 0; i < 10; i++) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }

        for (int i = 0; i < 10; i += 2) {
            assertTrue("Must be able to remove int", set.remove(Integer.toString(i)));
        }

        it = set.iterator();
        for (int i = 1; i < 10; i += 2) {
            assertEquals("Sequence is wrong after remove ", Integer.toString(i), it.next());
        }

        for (int i = 0; i < 10; i++) {
            set.add((E) Integer.toString(i));
        }

        assertEquals("Size of set is wrong!", 10, set.size());

        it = set.iterator();
        for (int i = 1; i < 10; i += 2) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
        for (int i = 0; i < 10; i += 2) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
    }

// org.apache.commons.collections.set.ListOrderedSet2Test::testListAddRemove
    public void testListAddRemove() {
        ListOrderedSet<E> set = makeObject();
        List<E> view = set.asList();
        set.add((E) ZERO);
        set.add((E) ONE);
        set.add((E) TWO);

        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));
        assertEquals(3, view.size());
        assertSame(ZERO, view.get(0));
        assertSame(ONE, view.get(1));
        assertSame(TWO, view.get(2));

        assertEquals(0, set.indexOf(ZERO));
        assertEquals(1, set.indexOf(ONE));
        assertEquals(2, set.indexOf(TWO));

        set.remove(1);
        assertEquals(2, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(TWO, set.get(1));
        assertEquals(2, view.size());
        assertSame(ZERO, view.get(0));
        assertSame(TWO, view.get(1));
    }

// org.apache.commons.collections.set.ListOrderedSet2Test::testListAddIndexed
    public void testListAddIndexed() {
        ListOrderedSet<E> set = makeObject();
        set.add((E) ZERO);
        set.add((E) TWO);

        set.add(1, (E) ONE);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        set.add(0, (E) ONE);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        List<E> list = new ArrayList<E>();
        list.add((E) ZERO);
        list.add((E) TWO);

        set.addAll(0, list);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        list.add(0, (E) THREE); 
        set.remove(TWO);    
        set.addAll(1, list);
        assertEquals(4, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(THREE, set.get(1));
        assertSame(TWO, set.get(2));
        assertSame(ONE, set.get(3));
    }

// org.apache.commons.collections.set.ListOrderedSetTest::testOrdering
    public void testOrdering() {
        ListOrderedSet<E> set = setupSet();
        Iterator<E> it = set.iterator();

        for (int i = 0; i < 10; i++) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }

        for (int i = 0; i < 10; i += 2) {
            assertTrue("Must be able to remove int",
                       set.remove(Integer.toString(i)));
        }

        it = set.iterator();
        for (int i = 1; i < 10; i += 2) {
            assertEquals("Sequence is wrong after remove ",
                         Integer.toString(i), it.next());
        }

        for (int i = 0; i < 10; i++) {
            set.add((E) Integer.toString(i));
        }

        assertEquals("Size of set is wrong!", 10, set.size());

        it = set.iterator();
        for (int i = 1; i < 10; i += 2) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
        for (int i = 0; i < 10; i += 2) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
    }

// org.apache.commons.collections.set.ListOrderedSetTest::testListAddRemove
    public void testListAddRemove() {
        ListOrderedSet<E> set = makeObject();
        List<E> view = set.asList();
        set.add((E) ZERO);
        set.add((E) ONE);
        set.add((E) TWO);

        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));
        assertEquals(3, view.size());
        assertSame(ZERO, view.get(0));
        assertSame(ONE, view.get(1));
        assertSame(TWO, view.get(2));

        assertEquals(0, set.indexOf(ZERO));
        assertEquals(1, set.indexOf(ONE));
        assertEquals(2, set.indexOf(TWO));

        set.remove(1);
        assertEquals(2, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(TWO, set.get(1));
        assertEquals(2, view.size());
        assertSame(ZERO, view.get(0));
        assertSame(TWO, view.get(1));
    }

// org.apache.commons.collections.set.ListOrderedSetTest::testListAddIndexed
    public void testListAddIndexed() {
        ListOrderedSet<E> set = makeObject();
        set.add((E) ZERO);
        set.add((E) TWO);

        set.add(1, (E) ONE);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        set.add(0, (E) ONE);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        List<E> list = new ArrayList<E>();
        list.add((E) ZERO);
        list.add((E) TWO);

        set.addAll(0, list);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        list.add(0, (E) THREE); 
        set.remove(TWO); 
        set.addAll(1, list);
        assertEquals(4, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(THREE, set.get(1));
        assertSame(TWO, set.get(2));
        assertSame(ONE, set.get(3));
    }

// org.apache.commons.collections.set.ListOrderedSetTest::testListAddReplacing
    public void testListAddReplacing() {
        ListOrderedSet<E> set = makeObject();
        A a = new A();
        B b = new B();
        set.add((E) a);
        assertEquals(1, set.size());
        set.add((E) b); 
        assertEquals(1, set.size());
        assertSame(a, set.decorated().iterator().next());
        assertSame(a, set.iterator().next());
        assertSame(a, set.get(0));
        assertSame(a, set.asList().get(0));
    }

// org.apache.commons.collections.set.ListOrderedSetTest::testRetainAll
    public void testRetainAll() {
        List<E> list = new ArrayList<E>(10);
        Set<E> set = new HashSet<E>(10);
        ListOrderedSet<E> orderedSet = ListOrderedSet.listOrderedSet(set, list);
        for (int i = 0; i < 10; ++i) {
            orderedSet.add((E) Integer.valueOf(10 - i - 1));
        }

        Collection<E> retained = new ArrayList<E>(5);
        for (int i = 0; i < 5; ++i) {
            retained.add((E) Integer.valueOf(i * 2));
        }

        assertTrue(orderedSet.retainAll(retained));
        assertEquals(5, orderedSet.size());
        
        assertEquals(Integer.valueOf(8), orderedSet.get(0));
        assertEquals(Integer.valueOf(6), orderedSet.get(1));
        assertEquals(Integer.valueOf(4), orderedSet.get(2));
        assertEquals(Integer.valueOf(2), orderedSet.get(3));
        assertEquals(Integer.valueOf(0), orderedSet.get(4));
    }

// org.apache.commons.collections.set.ListOrderedSetTest::testRetainAllCollections426
    public void testRetainAllCollections426() {
        int size = 100000;
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        for (int i = 0; i < size; i++) {
            set.add(i);
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = size; i < 2 * size; i++) {
            list.add(i);
        }

        long start = System.currentTimeMillis();
        set.retainAll(list);
        long stop = System.currentTimeMillis();

        
        
        
        assertTrue((stop - start) < 5000);
    }

// org.apache.commons.collections.set.ListOrderedSetTest::testDecorator
    public void testDecorator() {
        try {
            ListOrderedSet.listOrderedSet((List<E>) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            ListOrderedSet.listOrderedSet((Set<E>) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            ListOrderedSet.listOrderedSet(null, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            ListOrderedSet.listOrderedSet(new HashSet<E>(), null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            ListOrderedSet.listOrderedSet(null, new ArrayList<E>());
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }
