// org/jsoup/parser/HtmlParserTest.java
@Test public void preservedCaseFormattingElementsWithNestedSpecialElements() {
    String html = "<B><div>content</div></B>";
    Document doc = Parser.htmlParser()
        .settings(ParseSettings.preserveCase)
        .parseInput(html, "");
    assertEquals("<B></B> <div> content </div>", StringUtil.normaliseWhitespace(doc.body().html()));
}