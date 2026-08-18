// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void normalizesDiscordantTagsUppercaseStart() {
    Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
    Document document = Jsoup.parse("<DIV>test</div><p></p>", "", parser);
    assertEquals("<div>\n test\n</div>\n<p></p>", document.html());
}
