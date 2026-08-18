// org/apache/commons/collections/list/SetUniqueListTest.java
public void testSetSameElementNull() {
        final SetUniqueList<Integer> lset = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());
        final Integer obj1 = null;
        lset.add(obj1);
        lset.set(0, obj1);
        assertEquals(1, lset.size());
        assertNull(lset.get(0));
        assertTrue(lset.contains(obj1));
    }
