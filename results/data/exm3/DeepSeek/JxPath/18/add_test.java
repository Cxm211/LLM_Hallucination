// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testAxisAttributeWildcardSecondLocation() {
        assertXPathValueIterator(
            context,
            "vendor/location[2]/@*",
            set("101", "", "local"));
    }
