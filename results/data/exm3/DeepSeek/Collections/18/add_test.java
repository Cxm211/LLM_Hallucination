// org/apache/commons/collections/set/ListOrderedSetTest.java
public void testRetainAllPartial() {
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);
        set.add(5);
        List<Integer> coll = Arrays.asList(2, 4, 6);
        boolean result = set.retainAll(coll);
        assertTrue(result);
        assertEquals(2, set.size());
        // order should be preserved
        List<Integer> expectedOrder = Arrays.asList(2, 4);
        List<Integer> actualOrder = new ArrayList<Integer>(set);
        assertEquals(expectedOrder, actualOrder);
    }
