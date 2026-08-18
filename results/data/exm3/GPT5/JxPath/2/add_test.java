// org/apache/commons/jxpath/ri/compiler/ExtensionFunctionTest.java::testNodeSetReturnPointersBase
public void testNodeSetReturnPointersBase() {
        assertXPathPointerIterator(
            context,
            "test:nodeSet()",
            list("/beans[1]", "/beans[2]"));
    }