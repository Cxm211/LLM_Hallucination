// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAttributeNodeTypeTest() {
    assertXPathValueIterator(
        context,
        "vendor/location[1]/attribute::node()",
        set("100", "", "local"));
}