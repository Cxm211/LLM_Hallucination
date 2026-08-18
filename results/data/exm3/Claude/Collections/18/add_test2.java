// org/apache/commons/collections/set/ListOrderedSetTest.java
public void testRetainAllNoChange() {
    ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
    for (int i = 0; i < 100; i++) {
        set.add(i);
    }
    ArrayList<Integer> list = new ArrayList<Integer>();
    for (int i = 0; i < 100; i++) {
        list.add(i);
    }
    boolean result = set.retainAll(list);
    assertFalse(result);
    assertEquals(100, set.size());
}