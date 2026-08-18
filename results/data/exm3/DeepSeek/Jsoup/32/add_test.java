// org/jsoup/nodes/ElementTest.java
@Test public void testClonesClassnamesWhenNoClass() {
    Document doc = Jsoup.parse("<div></div>");
    Element div = doc.select("div").first();
    Set<String> classes = div.classNames();
    assertTrue(classes.isEmpty());
    
    Element copy = div.clone();
    Set<String> copyClasses = copy.classNames();
    assertTrue(copyClasses.isEmpty());
    copyClasses.add("newclass");
    
    assertTrue(classes.isEmpty());
    assertFalse(copyClasses.isEmpty());
    assertTrue(copyClasses.contains("newclass"));
}
