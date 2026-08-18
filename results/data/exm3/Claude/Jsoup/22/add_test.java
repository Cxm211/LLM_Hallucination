// org/jsoup/nodes/ElementTest.java
@Test public void siblingElementsWithSingleChild() {
    Document doc = Jsoup.parse("<div><p>Only</p></div>");
    Element p = doc.select("p").first();
    Elements siblings = p.siblingElements();
    assertEquals(0, siblings.size());
}