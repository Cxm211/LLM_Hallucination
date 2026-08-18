// org/apache/commons/jxpath/ri/model/ExternalXMLNamespaceTest.java
public void testGetPrefixWithUpdatedNamespace() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        JXPathContext context = JXPathContext.newContext(doc);
        NodePointer rootPtr = context.getPointer("/root");
        // First registration
        context.registerNamespace("foo", "http://example.com/ns1");
        String prefix1 = rootPtr.getPrefix("http://example.com/ns1");
        assertEquals("foo", prefix1);
        // Change registration for same prefix
        context.registerNamespace("foo", "http://example.com/ns2");
        // Old URI should no longer map to foo
        String prefix2 = rootPtr.getPrefix("http://example.com/ns1");
        assertNull(prefix2);
        // New URI should map to foo
        String prefix3 = rootPtr.getPrefix("http://example.com/ns2");
        assertEquals("foo", prefix3);
    }
