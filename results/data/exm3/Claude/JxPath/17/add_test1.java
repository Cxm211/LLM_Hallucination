// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAxisAttributeNamespaceFiltering() {
    // Test filtering attributes by namespace URI
    assertXPathValueIterator(
        context,
        "vendor/product/price:amount/@*[namespace-uri() = 'priceNS']",
        list("10%"));
}