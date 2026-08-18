// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAxisAttributeNamespaceWildcardMixed() {
    // Test wildcard with mixed namespace and no-namespace attributes
    assertXPathValueIterator(
        context,
        "vendor/location[2]/@*",
        set("101", "", "Tangerine Drive"));
}