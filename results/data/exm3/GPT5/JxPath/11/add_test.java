// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java::testNamespaceMapping
public void testNamespaceMapping_AttributeWildcard() {
        context.registerNamespace("rate", "priceNS");
        assertXPathValue(context,
                "count(vendor/product/rate:amount[1]/@rate:*)",
                new Double(1));
        assertXPathValue(context,
                "count(vendor/product/rate:amount[1]/@price:*)",
                new Double(1));
    }