// org/apache/commons/jxpath/ri/compiler/ExtensionFunctionTest.java
public void testNodeSetReturnSingleNode() {
    assertXPathPointerIterator(
        context,
        "test:singleNodeSet()/name",
        list("/beans[1]/name"));
}