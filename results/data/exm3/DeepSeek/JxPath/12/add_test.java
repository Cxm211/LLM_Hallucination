// org/apache/commons/jxpath/ri/model/ExternalXMLNamespaceTest.java
public void testAttributeNameTestDOM() throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    Element root = doc.createElementNS("http://example.com", "pre:root");
    doc.appendChild(root);
    Attr attr = doc.createAttributeNS("http://example.com", "pre:attr");
    root.setAttributeNode(attr);
    
    QName qname = new QName("pre", "attr");
    NodeNameTest test = new NodeNameTest(qname, "http://example.com");
    
    boolean result = DOMNodePointer.testNode(attr, test);
    assertTrue("Attribute should match NodeNameTest", result);
}
