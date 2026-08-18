// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAttributeWildcardWithNamespace() {
    context.registerNamespace("rate", "priceNS");
    assertXPathValue(context,
            "count(vendor[1]/product[1]/rate:amount[1]/@rate:*)", new Double(1));
}