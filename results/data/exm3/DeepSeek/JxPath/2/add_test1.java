// org/apache/commons/jxpath/ri/compiler/ExtensionFunctionTest.java
public void testSimpleValueReturnNoCurrentNode() {
        // Create a context with no current node pointer
        JXPathContext noPointerContext = JXPathContext.newContext(null);
        noPointerContext.setFunctions(new TestFunctions());
        // Value iteration should work
        assertXPathValueIterator(
            noPointerContext,
            "test:simpleValue()",
            list("hello"));
        // Pointer iteration should not throw NPE
        assertXPathPointerIterator(
            noPointerContext,
            "test:simpleValue()",
            list("/value[1]"));
    }
