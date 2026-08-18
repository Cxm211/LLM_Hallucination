// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNodeSetBoundaryComparisons() {
    assertXPathValue(context, "$array > 0.75", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$array >= 0.75", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "$array < 0.25", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$array <= 0.25", Boolean.TRUE, Boolean.class);
}