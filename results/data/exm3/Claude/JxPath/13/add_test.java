// org/apache/commons/jxpath/ri/model/ExternalXMLNamespaceTest.java
public void testGetPrefixWithNullPointer() {
    JXPathContext context = JXPathContext.newContext(null);
    context.registerNamespace("ns", "http://example.com/ns");
    String prefix = context.getPrefix("http://example.com/ns");
    assertEquals("ns", prefix);
}