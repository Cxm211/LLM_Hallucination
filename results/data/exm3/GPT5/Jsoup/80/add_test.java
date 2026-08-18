// org/jsoup/parser/XmlTreeBuilderTest.java::handlesDodgyBangDecl
@Test
public void handlesDodgyBangDecl() {
    String xml = "<!foo a='1'><val>One</val>";
    Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
    assertEquals("One", doc.select("val").text());
}