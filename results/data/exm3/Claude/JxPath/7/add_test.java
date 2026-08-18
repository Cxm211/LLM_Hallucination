// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNodeSetOperationsReversed() {
    assertXPathValue(context, "0 < $array", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "0 <= $array", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "1 > $array", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "1 >= $array", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "1 < $array", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "0 > $array", Boolean.FALSE, Boolean.class);
}