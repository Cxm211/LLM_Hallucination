// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void testXmlDeclarationWithOnlyVersion() {
    String xml = "<?xml version='1.0'?><root>content</root>";
    Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
    XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
    assertEquals("1.0", decl.attr("version"));
    assertEquals("", decl.attr("encoding"));
    assertEquals("xml version=\"1.0\"", decl.getWholeDeclaration());
    assertEquals("<?xml version=\"1.0\"?>", decl.outerHtml());
}