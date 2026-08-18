// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void convertsCommentedXmlDeclToDeclaration() {
    String html = "<!--?xml version=\"1.0\"?-->";
    Document doc = Jsoup.parse(html, "", Parser.xmlParser());
    assertEquals("<?xml version=\"1.0\"?>", doc.outerHtml());
}
