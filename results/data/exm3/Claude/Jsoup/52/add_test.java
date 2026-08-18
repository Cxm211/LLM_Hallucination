// org/jsoup/nodes/DocumentTest.java
@Test
public void testMetaCharsetUpdateXmlMultipleAttributes() {
    final Document doc = createXmlDocument("1.0", "UTF-8", true);
    XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
    decl.attr("standalone", "yes");
    doc.updateMetaCharsetElement(true);
    doc.charset(Charset.forName("ISO-8859-1"));
    
    String result = doc.toString();
    assertTrue(result.contains("version=\"1.0\""));
    assertTrue(result.contains("encoding=\"ISO-8859-1\""));
    assertTrue(result.contains("standalone=\"yes\""));
    
    XmlDeclaration selectedNode = (XmlDeclaration) doc.childNode(0);
    assertEquals("ISO-8859-1", selectedNode.attr("encoding"));
    assertEquals("yes", selectedNode.attr("standalone"));
}