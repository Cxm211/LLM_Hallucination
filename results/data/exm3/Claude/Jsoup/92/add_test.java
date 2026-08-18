// org/jsoup/parser/HtmlParserTest.java
@Test public void dropsDuplicateAttributesWithEmptyValues() {
    String html = "<p attr attr=\"value\" attr>Text</p>";
    Parser parser = Parser.htmlParser().setTrackErrors(10);
    Document doc = parser.parseInput(html, "");

    Element p = doc.selectFirst("p");
    assertEquals("<p attr>Text</p>", p.outerHtml());

    assertTrue(parser.getErrors().size() >= 1);
}