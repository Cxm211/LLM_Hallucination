// org/jsoup/nodes/ElementTest.java
@Test public void testCloneClassNamesIndependence() {
    Document doc = Jsoup.parse("<div class='alpha'></div>");
    Element div = doc.select("div").first();
    Set<String> classes = div.classNames();
    assertEquals(1, classes.size());

    Element copy = div.clone();
    Set<String> copyClasses = copy.classNames();
    assertEquals(1, copyClasses.size());
    
    copyClasses.clear();
    copyClasses.add("beta");
    copyClasses.add("gamma");

    assertEquals(1, classes.size());
    assertTrue(classes.contains("alpha"));
    assertEquals(2, copyClasses.size());
    assertTrue(copyClasses.contains("beta"));
    assertTrue(copyClasses.contains("gamma"));
    assertFalse(copyClasses.contains("alpha"));
}