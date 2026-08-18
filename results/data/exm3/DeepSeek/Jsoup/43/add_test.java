// org/jsoup/nodes/ElementTest.java
@Test public void testIndexInListNotFound() throws Exception {
    Method indexInList = Element.class.getDeclaredMethod("indexInList", Element.class, List.class);
    indexInList.setAccessible(true);
    
    Element search = new Element(Tag.valueOf("div"), "");
    
    List<Element> empty = new ArrayList<>();
    Integer result1 = (Integer) indexInList.invoke(null, search, empty);
    assertEquals(Integer.valueOf(-1), result1);
    
    List<Element> list = new ArrayList<>();
    list.add(new Element(Tag.valueOf("p"), ""));
    Integer result2 = (Integer) indexInList.invoke(null, search, list);
    assertEquals(Integer.valueOf(-1), result2);
}
