// org/jsoup/parser/HtmlParserTest.java
@Test public void mixedSelfClosingVoidAndNonVoid() {
    String html = "<br /><div /><img />";
    Parser parser = Parser.htmlParser().setTrackErrors(5);
    parser.parseInput(html, "");
    assertEquals(1, parser.getErrors().size());
    assertEquals("13: Tag cannot be self closing; not a void tag", parser.getErrors().get(0).toString());
}