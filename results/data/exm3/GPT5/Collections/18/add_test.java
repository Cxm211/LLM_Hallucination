// org/apache/commons/collections/set/ListOrderedSetTest.java::testRetainAllCollectionsSameElements
public void testRetainAllCollectionsSameElements() {
        int size = 100000;
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        for (int i = 0; i < size; i++) {
            set.add(i);
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }

        long start = System.currentTimeMillis();
        boolean changed = set.retainAll(list);
        long stop = System.currentTimeMillis();

        assertFalse(changed);
        assertEquals(size, set.size());
        assertTrue((stop - start) < 5000);
    }