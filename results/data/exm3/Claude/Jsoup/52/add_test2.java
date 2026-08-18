// org/jsoup/parser/XmlTreeBuilderTest.java
@Test
public void testParseByteDataWithNullCharset() throws Exception {
    String xmlContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root>test</root>";
    ByteBuffer buffer = ByteBuffer.wrap(xmlContent.getBytes("ISO-8859-1"));
    Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.xmlParser());
    assertEquals("ISO-8859-1", doc.charset().name());
    XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
    assertEquals("ISO-8859-1", decl.attr("encoding"));
}