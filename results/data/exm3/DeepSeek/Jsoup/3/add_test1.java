// org/jsoup/nodes/ElementTest.java
@Test public void testWrapWithNoElement() {
    Document doc = Jsoup.parse("<div>Test</div>");
    Element div = doc.select("div").first();
    Element result = div.wrap("<!-- comment -->");
    assertNotNull(result);
    assertSame(div, result);
}
