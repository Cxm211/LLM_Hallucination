// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void testXmlDeclarationVersionOnly() {
    String xml = "<?xml version='1.0'?><root/>";
    Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
    XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
    assertEquals("1.0", decl.attr("version"));
    assertEquals("xml version=\"1.0\"", decl.getWholeDeclaration());
    assertEquals("<?xml version=\"1.0\"?>", decl.outerHtml());
}
