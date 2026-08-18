// org/jsoup/nodes/EntitiesTest.java
@Test public void escapesLtGtInXmlNonAttributeContent() {
    String docHtml = "<div><>test</></div>";
    Document doc = Jsoup.parse(docHtml);
    Element element = doc.select("div").first();

    doc.outputSettings().escapeMode(EscapeMode.base);
    String htmlOut = element.html();
    assertTrue(htmlOut.contains("&lt;") && htmlOut.contains("&gt;"));

    doc.outputSettings().escapeMode(EscapeMode.xhtml);
    String xhtmlOut = element.html();
    assertTrue(xhtmlOut.contains("&lt;") && xhtmlOut.contains("&gt;"));
}