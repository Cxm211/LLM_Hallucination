// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testComparisons() {
    // Test boolean comparisons
    assertXPathValue(context, "$trueVar = $trueVar", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "$trueVar != $trueVar", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$trueVar = $falseVar", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$trueVar != $falseVar", Boolean.TRUE, Boolean.class);
    // Test number comparisons (non-NaN)
    assertXPathValue(context, "$numVar = $numVar", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "$numVar != $numVar", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$num1 = $num2", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$num1 != $num2", Boolean.TRUE, Boolean.class);
    // Test string comparisons
    assertXPathValue(context, "$strVar = $strVar", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "$strVar != $strVar", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$str1 = $str2", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$str1 != $str2", Boolean.TRUE, Boolean.class);
}
