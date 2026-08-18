// org/apache/commons/jxpath/ri/model/JXPath154Test.java
public void testGetNamespaceURI_AttributeWithPrefix() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    Element root = doc.createElementNS("http://example.com", "root");
    doc.appendChild(root);
    Attr attr = doc.createAttributeNS("http://example.com", "pre:attr");
    root.setAttributeNode(attr);
    String ns = NodePointer.getNamespaceURI(attr);
    assertEquals("http://example.com", ns);
}
