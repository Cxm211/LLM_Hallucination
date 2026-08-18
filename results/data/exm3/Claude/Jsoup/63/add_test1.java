// org/jsoup/parser/HtmlParserTest.java
@Test public void multipleSelfClosingNonVoidTags() {
    String html = "<div /><span /><p />";
    Parser parser = Parser.htmlParser().setTrackErrors(10);
    parser.parseInput(html, "");
    assertEquals(3, parser.getErrors().size());
    assertEquals("6: Tag cannot be self closing; not a void tag", parser.getErrors().get(0).toString());
    assertEquals("14: Tag cannot be self closing; not a void tag", parser.getErrors().get(1).toString());
    assertEquals("21: Tag cannot be self closing; not a void tag", parser.getErrors().get(2).toString());
}