// org/jsoup/parser/HtmlParserTest.java
@Test public void selfClosingVoidAndNonVoidMix() {
    String html = "<br /><div />";
    Parser parser = Parser.htmlParser().setTrackErrors(5);
    parser.parseInput(html, "");
    assertEquals(1, parser.getErrors().size());
    assertTrue(parser.getErrors().get(0).toString().contains("Tag cannot be self closing; not a void tag"));
    String clean = Jsoup.clean(html, Whitelist.relaxed());
    assertEquals("<br> <div></div>", StringUtil.normaliseWhitespace(clean));
}
