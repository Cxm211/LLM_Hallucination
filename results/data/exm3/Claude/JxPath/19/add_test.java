// org/apache/commons/jxpath/ri/model/AliasedNamespaceIterationTest.java
public void testIterateDOMWithNoMatchingElements() {
    try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElementNS("http://example.com", "root");
        doc.appendChild(root);
        Element child1 = doc.createElementNS("http://example.com", "different1");
        root.appendChild(child1);
        Element child2 = doc.createElementNS("http://example.com", "different2");
        root.appendChild(child2);
        Element target = doc.createElementNS("http://example.com", "unique");
        root.appendChild(target);
        
        JXPathContext context = JXPathContext.newContext(new DocumentContainer(doc, DocumentContainer.MODEL_DOM));
        Iterator it = context.iteratePointers("//*[local-name()='unique']");
        assertTrue("Should find the unique element", it.hasNext());
        Pointer ptr = (Pointer) it.next();
        assertEquals("Position should be 1", "unique[1]", ptr.asPath().substring(ptr.asPath().lastIndexOf('/') + 1));
    } catch (Exception e) {
        fail("Exception during test: " + e.getMessage());
    }
}