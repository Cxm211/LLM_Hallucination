// org/jsoup/parser/HtmlParserTest.java
@Test public void caseSensitiveEndTagNonSpecial() {
    String html = "<Div>Content</Div>";
    Parser parser = Parser.htmlParser();
    parser.settings(ParseSettings.preserveCase);
    Document doc = parser.parseInput(html, "");
    assertEquals("<Div> Content </Div>", StringUtil.normaliseWhitespace(doc.body().html()));
}
