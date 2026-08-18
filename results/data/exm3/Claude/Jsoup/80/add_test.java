// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void handlesXmlDeclWithClosingQuestionMark() {
    String xml = "<?xml version='1.0' encoding='UTF-8'?><root><val>Test</val></root>";
    Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
    assertEquals("Test", doc.select("val").text());
}