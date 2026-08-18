// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java::testAxisAttribute
public void testAxisAttribute_NamespacedOnlyWithWildcard() {
        // @* with predicate selecting only namespaced attributes
        assertXPathValueIterator(
            context,
            "vendor/product/price:amount/@*[namespace-uri() != '']",
            list("10%"));
    }