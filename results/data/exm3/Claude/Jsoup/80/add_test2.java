// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void handlesXmlDeclWithAttributes() {
    String xml = "<?xml version='1.0' standalone='yes'?><data>Value</data>";
    Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
    assertEquals("Value", doc.select("data").text());
}