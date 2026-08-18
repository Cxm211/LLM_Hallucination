// org/apache/commons/collections/set/ListOrderedSetTest.java
public void testRetainAllPartialOverlap() {
    ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
    for (int i = 0; i < 1000; i++) {
        set.add(i);
    }
    ArrayList<Integer> list = new ArrayList<Integer>();
    for (int i = 500; i < 1500; i++) {
        list.add(i);
    }
    boolean result = set.retainAll(list);
    assertTrue(result);
    assertEquals(500, set.size());
    for (int i = 500; i < 1000; i++) {
        assertTrue(set.contains(i));
    }
    for (int i = 0; i < 500; i++) {
        assertFalse(set.contains(i));
    }
}