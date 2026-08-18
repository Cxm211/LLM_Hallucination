// org/jsoup/nodes/ElementTest.java
@Test public void siblingElementsLastChild() {
    Document doc = Jsoup.parse("<div><p>First</p><p>Second</p><p>Third</p></div>");
    Element p3 = doc.select("p").get(2);
    Elements siblings = p3.siblingElements();
    assertEquals(2, siblings.size());
    assertEquals("<p>First</p>", siblings.get(0).outerHtml());
    assertEquals("<p>Second</p>", siblings.get(1).outerHtml());
}