// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNormalNumbersEqual() {
    assertXPathValue(context, "5 = 5", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "5 != 5", Boolean.FALSE, Boolean.class);
}