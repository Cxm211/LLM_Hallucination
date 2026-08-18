// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void testXmlDeclarationWithStandalone() {
    String xml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><root/>";
    Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
    XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
    assertEquals("1.0", decl.attr("version"));
    assertEquals("UTF-8", decl.attr("encoding"));
    assertEquals("yes", decl.attr("standalone"));
    assertEquals("xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"", decl.getWholeDeclaration());
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", decl.outerHtml());
}
