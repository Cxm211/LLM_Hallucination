// org/apache/commons/collections/list/SetUniqueListTest.java
public void testSetWithDifferentObjects() {
    final SetUniqueList<Integer> lset = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());
    
    final Integer obj1 = new Integer(1);
    final Integer obj2 = new Integer(2);
    final Integer obj3 = new Integer(3);
    
    lset.add(obj1);
    lset.add(obj2);
    lset.add(obj3);
    
    // Set index 1 to obj1, which already exists at index 0
    lset.set(1, obj1);
    
    assertEquals(2, lset.size());
    assertSame(obj1, lset.get(0));
    assertSame(obj1, lset.get(1));
    
    assertTrue(lset.contains(obj1));
    assertFalse(lset.contains(obj2));
    assertTrue(lset.contains(obj3));
}