// org/apache/commons/jxpath/ri/compiler/ExtensionFunctionTest.java
public void testNullReturn() {
        // extension function that returns null
        assertXPathValueIterator(
            context,
            "test:nullFunction()",
            Collections.EMPTY_LIST);
        assertXPathPointerIterator(
            context,
            "test:nullFunction()",
            Collections.EMPTY_LIST);
    }
