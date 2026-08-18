// org/jsoup/nodes/ElementTest.java
@Test public void testCloneWithNoClassNames() {
    Document doc = Jsoup.parse("<div></div>");
    Element div = doc.select("div").first();
    Set<String> classes = div.classNames();
    assertEquals(0, classes.size());

    Element copy = div.clone();
    Set<String> copyClasses = copy.classNames();
    assertEquals(0, copyClasses.size());
    copyClasses.add("newclass");

    assertEquals(0, classes.size());
    assertEquals(1, copyClasses.size());
    assertTrue(copyClasses.contains("newclass"));
    assertFalse(classes.contains("newclass"));
}