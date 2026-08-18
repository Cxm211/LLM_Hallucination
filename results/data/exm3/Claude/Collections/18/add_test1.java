// org/apache/commons/collections/set/ListOrderedSetTest.java
public void testRetainAllEmptyCollection() {
    ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
    for (int i = 0; i < 100; i++) {
        set.add(i);
    }
    ArrayList<Integer> list = new ArrayList<Integer>();
    boolean result = set.retainAll(list);
    assertTrue(result);
    assertEquals(0, set.size());
}