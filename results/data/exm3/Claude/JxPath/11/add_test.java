// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAttributeNamespaceWithExternalPrefix() {
    context.registerNamespace("ext", "priceNS");
    assertXPathValue(context,
            "vendor[1]/product[1]/price:amount[1]/@ext:discount", "10%");
}