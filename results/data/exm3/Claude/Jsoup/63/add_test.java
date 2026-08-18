// org/jsoup/parser/HtmlParserTest.java
@Test public void selfClosingUnknownTagNoError() {
    String html = "<unknown />";
    Parser parser = Parser.htmlParser().setTrackErrors(5);
    parser.parseInput(html, "");
    assertEquals(0, parser.getErrors().size());
}