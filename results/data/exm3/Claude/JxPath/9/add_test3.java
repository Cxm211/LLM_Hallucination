// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNormalNumbersNotEqual() {
    assertXPathValue(context, "5 = 6", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "5 != 6", Boolean.TRUE, Boolean.class);
}