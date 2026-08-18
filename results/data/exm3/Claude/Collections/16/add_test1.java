// org/apache/commons/collections/list/TestSetUniqueList.java
public void testSubListContainsAll() {
    List list = new ArrayList();
    List uniqueList = SetUniqueList.decorate(list);
    
    uniqueList.add("X");
    uniqueList.add("Y");
    uniqueList.add("Z");
    
    List subList = uniqueList.subList(0, 2);
    
    List checkList = new ArrayList();
    checkList.add("X");
    checkList.add("Y");
    assertTrue(subList.containsAll(checkList));
    
    checkList.add("Z");
    assertFalse(subList.containsAll(checkList));
    
    assertFalse(subList.contains("Z"));
}