// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAttributeWithNamespaceWildcardMultiple() {
    assertXPathValueIterator(
        context,
        "vendor/product/price:amount/attribute::price:*",
        list("10%"));
}