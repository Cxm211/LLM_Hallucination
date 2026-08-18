// org/jsoup/parser/HtmlParserTest.java
@Test public void duplicateAttributesSelfClosing() {
    String html = "<img src=foo src=bar />";
    Parser parser = Parser.htmlParser().setTrackErrors(10);
    Document doc = parser.parseInput(html, "");
    Element img = doc.selectFirst("img");
    assertEquals("<img src=\"foo\">", img.outerHtml());
    assertEquals(1, parser.getErrors().size());
    assertEquals("Duplicate attribute", parser.getErrors().get(0).getErrorMessage());
}
