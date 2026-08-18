// org/apache/commons/collections/list/SetUniqueListTest.java
public void testSetEqualButNotSame() {
        final SetUniqueList<Integer> lset = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());
        final Integer obj1 = new Integer(1);
        final Integer obj2 = new Integer(1);
        lset.add(obj1);
        lset.set(0, obj2);
        assertEquals(1, lset.size());
        assertSame(obj2, lset.get(0));
        assertTrue(lset.contains(obj2));
    }
