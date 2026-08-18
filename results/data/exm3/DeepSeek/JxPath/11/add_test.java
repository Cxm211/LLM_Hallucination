// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAttributeNamespaceFromContext() {
    Element root = new Element("root");
    Namespace docNs = Namespace.getNamespace("doc", "http://example.com/ns");
    root.setAttribute("attr", "value", docNs);
    
    JXPathContext context = JXPathContext.newContext(root);
    context.registerNamespace("ctx", "http://example.com/ns");
    
    assertXPathValue(context, "//@ctx:attr", "value");
    assertXPathValue(context, "count(//@ctx:*)", new Double(1));
    
    root.setAttribute("plain", "plainValue");
    assertXPathValue(context, "//@plain", "plainValue");
}
