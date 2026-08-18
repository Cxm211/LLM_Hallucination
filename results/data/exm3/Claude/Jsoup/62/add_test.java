// org/jsoup/parser/HtmlParserTest.java
@Test public void caseSensitiveEndTagMismatchSpecialElement() {
    String html = "<r><div><X>A</div></X></r>";
    Parser parser = Parser.htmlParser();
    parser.settings(ParseSettings.preserveCase);
    Document doc = parser.parseInput(html, "");
    assertEquals("<r> <div> <X> A </X> </div> </r>", StringUtil.normaliseWhitespace(doc.body().html()));
}