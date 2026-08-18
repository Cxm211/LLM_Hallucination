// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void normalizesNestedDiscordantTags() {
    Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
    Document document = Jsoup.parse("<div><DIV>test</div></DIV>", "", parser);
    assertEquals("<div>\n <div>test</div>\n</div>", document.html());
}
