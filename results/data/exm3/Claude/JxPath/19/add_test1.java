// org/apache/commons/jxpath/ri/model/AliasedNamespaceIterationTest.java
public void testIterateJDOMWithMultipleSameNameElements() {
    try {
        Element root = new Element("root", Namespace.getNamespace("http://example.com"));
        Element item1 = new Element("item", Namespace.getNamespace("http://example.com"));
        Element item2 = new Element("item", Namespace.getNamespace("http://example.com"));
        Element item3 = new Element("item", Namespace.getNamespace("http://example.com"));
        root.addContent(item1);
        root.addContent(item2);
        root.addContent(item3);
        org.jdom.Document doc = new org.jdom.Document(root);
        
        JXPathContext context = JXPathContext.newContext(new DocumentContainer(doc, DocumentContainer.MODEL_JDOM));
        Iterator it = context.iteratePointers("//*[local-name()='item']");
        int count = 1;
        while (it.hasNext()) {
            Pointer ptr = (Pointer) it.next();
            String path = ptr.asPath();
            assertTrue("Path should contain item[" + count + "]", path.contains("item[" + count + "]"));
            count++;
        }
        assertEquals("Should have found 3 items", 4, count);
    } catch (Exception e) {
        fail("Exception during test: " + e.getMessage());
    }
}