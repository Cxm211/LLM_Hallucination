// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNanWithNonZeroValues() {
    assertXPathValue(context, "$nan >= $nan", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "$nan <= $nan", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "$nan != $nan", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$nan >= 5", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$nan <= 5", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$nan != 5", Boolean.TRUE, Boolean.class);
    assertXPathValue(context, "5 > $nan", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "5 < $nan", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "5 = $nan", Boolean.FALSE, Boolean.class);
}