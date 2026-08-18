// org/apache/commons/jxpath/ri/compiler/CoreFunctionTest.java
public void testFloorCeilingNegativeZero() {
    assertXPathValue(context, "floor(-0.5)", new Double(-1));
    assertXPathValue(context, "floor(-0.1)", new Double(-1));
    assertXPathValue(context, "ceiling(0.1)", new Double(1));
    assertXPathValue(context, "ceiling(0.9)", new Double(1));
}