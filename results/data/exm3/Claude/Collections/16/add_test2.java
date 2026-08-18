// org/apache/commons/collections/list/TestSetUniqueList.java
public void testSubListWithTreeSet() {
    List list = new ArrayList();
    List uniqueList = new SetUniqueList307(list, new java.util.TreeSet());
    
    uniqueList.add("Apple");
    uniqueList.add("Banana");
    uniqueList.add("Cherry");
    
    List subList = uniqueList.subList(1, 2);
    
    assertTrue(subList.contains("Banana"));
    assertFalse(subList.contains("Apple"));
    assertFalse(subList.contains("Cherry"));
    
    // Test add with TreeSet backing
    assertTrue(subList.add("Date"));
    assertFalse(subList.add("Banana"));
}