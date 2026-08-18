// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void handlesNestedMixedCaseTags() {
    Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
    Document document = Jsoup.parse("<Outer><Inner>text</INNER></OUTER>", "", parser);
    assertEquals("<outer>\n <inner>\n  text\n </inner>\n</outer>", document.html());
}