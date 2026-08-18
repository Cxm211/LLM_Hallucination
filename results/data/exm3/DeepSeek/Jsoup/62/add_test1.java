// org/jsoup/parser/HtmlParserTest.java
@Test public void caseSensitiveEndTagSpecial() {
    String html = "<Script>Code</Script>";
    Parser parser = Parser.htmlParser();
    parser.settings(ParseSettings.preserveCase);
    Document doc = parser.parseInput(html, "");
    assertEquals("<Script> Code </Script>", StringUtil.normaliseWhitespace(doc.body().html()));
}
