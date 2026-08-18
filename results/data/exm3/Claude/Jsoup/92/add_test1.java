// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void dropsDuplicateAttributesWithNullValues() {
    String html = "<p attr attr=\"val\" attr>Text</p>";
    Parser parser = Parser.xmlParser().setTrackErrors(10);
    Document doc = parser.parseInput(html, "");

    Element p = doc.selectFirst("p");
    assertEquals("<p attr>Text</p>", p.outerHtml());
}