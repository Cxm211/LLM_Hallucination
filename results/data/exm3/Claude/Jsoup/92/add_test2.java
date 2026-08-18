// org/jsoup/parser/HtmlParserTest.java
@Test public void dropsDuplicateAttributesMixedCase() {
    String html = "<p ABC=\"1\" abc=\"2\" Abc=\"3\">Text</p>";
    Parser parser = Parser.htmlParser().setTrackErrors(10);
    Document doc = parser.parseInput(html, "");

    Element p = doc.selectFirst("p");
    assertEquals("<p abc=\"1\">Text</p>", p.outerHtml());

    assertEquals(2, parser.getErrors().size());
}