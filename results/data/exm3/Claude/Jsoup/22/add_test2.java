// org/jsoup/nodes/ElementTest.java
@Test public void siblingElementsFirstChild() {
    Document doc = Jsoup.parse("<div><p>First</p><p>Second</p><p>Third</p></div>");
    Element p1 = doc.select("p").get(0);
    Elements siblings = p1.siblingElements();
    assertEquals(2, siblings.size());
    assertEquals("<p>Second</p>", siblings.get(0).outerHtml());
    assertEquals("<p>Third</p>", siblings.get(1).outerHtml());
}