// org/jsoup/parser/HtmlParserTest.java
@Test public void multipleSelfClosingNonVoidTags() {
    String html = "<div /><span />";
    Parser parser = Parser.htmlParser().setTrackErrors(5);
    parser.parseInput(html, "");
    assertEquals(2, parser.getErrors().size());
    for (ParseError error : parser.getErrors()) {
        assertTrue(error.toString().contains("Tag cannot be self closing; not a void tag"));
    }
    String clean = Jsoup.clean(html, Whitelist.relaxed());
    assertEquals("<div></div> <span></span>", StringUtil.normaliseWhitespace(clean));
}
