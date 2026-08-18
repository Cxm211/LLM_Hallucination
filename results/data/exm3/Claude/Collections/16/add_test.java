// org/apache/commons/collections/list/TestSetUniqueList.java
public void testSubListAdd() {
    List list = new ArrayList();
    List uniqueList = SetUniqueList.decorate(list);
    
    uniqueList.add("A");
    uniqueList.add("B");
    uniqueList.add("C");
    
    List subList = uniqueList.subList(1, 3);
    
    // Test that adding to sublist respects uniqueness within sublist
    assertTrue(subList.add("D"));
    assertTrue(subList.contains("D"));
    
    // Adding duplicate should fail
    assertFalse(subList.add("B"));
    assertEquals(3, subList.size());
    
    // Check parent list is updated
    assertEquals(4, uniqueList.size());
    assertTrue(uniqueList.contains("D"));
}