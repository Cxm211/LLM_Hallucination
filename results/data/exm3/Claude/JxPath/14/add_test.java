// org/apache/commons/jxpath/ri/compiler/CoreFunctionTest.java
public void testRoundEdgeCases() {
    assertXPathValue(context, "round(0.5)", new Double(1));
    assertXPathValue(context, "round(-0.5)", new Double(0));
    assertXPathValue(context, "round(2.5)", new Double(3));
    assertXPathValue(context, "round(-2.5)", new Double(-2));
    assertXPathValue(context, "round(0.4)", new Double(0));
    assertXPathValue(context, "round(-0.4)", new Double(0));
}