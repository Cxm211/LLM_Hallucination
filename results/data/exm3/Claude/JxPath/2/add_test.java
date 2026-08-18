// org/apache/commons/jxpath/ri/compiler/ExtensionFunctionTest.java
public void testNodeSetReturnNull() {
    assertXPathPointerIterator(
        context,
        "test:nullNodeSet()",
        Collections.EMPTY_LIST);
}