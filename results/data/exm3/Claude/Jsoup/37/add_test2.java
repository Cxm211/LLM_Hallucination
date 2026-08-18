// org/jsoup/nodes/ElementTest.java
@Test public void testPrettyPrintStillTrims() {
    Document doc = Jsoup.parse("<div>   <p>Test</p>   </div>");
    doc.outputSettings().prettyPrint(true);
    Element div = doc.select("div").first();
    String html = div.html();
    assertTrue(html.trim().equals(html));
}