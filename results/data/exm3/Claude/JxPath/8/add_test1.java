// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNanWithNegativeValues() {
    assertXPathValue(context, "$nan > -1", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$nan < -1", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$nan = -1", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "-1 > $nan", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "-1 < $nan", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "-1 = $nan", Boolean.FALSE, Boolean.class);
}