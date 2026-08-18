// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNanNotEqualToZero() {
    assertXPathValue(context, "$nan != 0", Boolean.TRUE, Boolean.class);
}