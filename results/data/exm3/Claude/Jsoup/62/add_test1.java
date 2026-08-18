// org/jsoup/parser/HtmlParserTest.java
@Test public void caseSensitiveNestedTags() {
    String html = "<r><A><B><C>text</C></B></A></r>";
    Parser parser = Parser.htmlParser();
    parser.settings(ParseSettings.preserveCase);
    Document doc = parser.parseInput(html, "");
    assertEquals("<r> <A> <B> <C> text </C> </B> </A> </r>", StringUtil.normaliseWhitespace(doc.body().html()));
}