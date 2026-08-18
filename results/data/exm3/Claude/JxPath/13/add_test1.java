// org/apache/commons/jxpath/ri/model/ExternalXMLNamespaceTest.java
public void testGetNamespaceURIWithParent() {
    DocumentContainer container = new DocumentContainer("<root xmlns:test='http://test.com'><child/></root>", DocumentContainer.MODEL_DOM);
    JXPathContext parent = JXPathContext.newContext(container);
    parent.registerNamespace("parent", "http://parent.com");
    JXPathContext child = JXPathContext.newContext(parent, container.getValue());
    String uri = child.getNamespaceURI("parent");
    assertEquals("http://parent.com", uri);
}