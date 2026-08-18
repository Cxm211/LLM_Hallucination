// org/apache/commons/jxpath/ri/model/ExternalXMLNamespaceTest.java
public void testNodeNodeTestDOM() throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);
    Attr attr = doc.createAttribute("attr");
    root.setAttributeNode(attr);
    Text text = doc.createTextNode("text");
    root.appendChild(text);
    Comment comment = doc.createComment("comment");
    root.appendChild(comment);
    
    NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
    
    assertTrue("Attribute should match node() test", DOMNodePointer.testNode(attr, test));
    assertTrue("Text node should match node() test", DOMNodePointer.testNode(text, test));
    assertTrue("Comment node should match node() test", DOMNodePointer.testNode(comment, test));
    assertTrue("Element node should match node() test", DOMNodePointer.testNode(root, test));
    assertTrue("Document node should match node() test", DOMNodePointer.testNode(doc, test));
}
