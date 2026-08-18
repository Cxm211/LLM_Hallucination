// org/jsoup/nodes/ElementTest.java
@Test public void testNotPrettyWithTrailingWhitespace() {
    Document doc = Jsoup.parse("<div><p>Test</p>   </div>");
    doc.outputSettings().prettyPrint(false);
    Element div = doc.select("div").first();
    assertEquals("<p>Test</p>   ", div.html());
}