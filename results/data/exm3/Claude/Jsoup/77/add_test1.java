// org/jsoup/parser/XmlTreeBuilderTest.java
@Test public void handlesMixedCaseWithSiblings() {
    Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
    Document document = Jsoup.parse("<A>first</a><B>second</b>", "", parser);
    assertEquals("<a>\n first\n</a>\n<b>\n second\n</b>", document.html());
}