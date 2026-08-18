// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAxisAttributeNamespaceURI() {
        // Additional test for namespace URI of attribute without prefix
        assertXPathValue(
            context,
            "namespace-uri(vendor/location/@manager)",
            "");
    }
