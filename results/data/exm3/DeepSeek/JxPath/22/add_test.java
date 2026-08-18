// org/apache/commons/jxpath/ri/model/JXPath154Test.java
public void testGetNamespaceURI_DocumentNoRoot() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    String ns = NodePointer.getNamespaceURI(doc);
    assertNull(ns);
}
