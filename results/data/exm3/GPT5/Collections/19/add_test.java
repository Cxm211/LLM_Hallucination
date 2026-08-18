// org/apache/commons/collections/list/SetUniqueListTest.java::testSetSameIndexEqualButDifferentInstance
public void testSetSameIndexEqualButDifferentInstance() {
        final SetUniqueList<Integer> lset = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());

        final Integer obj1 = new Integer(1);
        final Integer obj2 = new Integer(2);
        final Integer replacement = new Integer(1); // equal to obj1, different instance

        lset.add(obj1);
        lset.add(obj2);

        lset.set(0, replacement);

        assertEquals(2, lset.size());
        assertEquals(replacement, lset.get(0));
        assertSame(obj2, lset.get(1));
        assertTrue(lset.contains(replacement));
        assertTrue(lset.contains(obj2));
    }