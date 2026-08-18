// org/apache/commons/jxpath/ri/compiler/JXPath149Test.java
public void testNaNWithNumberComparison() {
    JXPathContext context = JXPathContext.newContext(null);
    context.getVariables().declareVariable("nan", Double.NaN);
    context.getVariables().declareVariable("num", Integer.valueOf(5));
    assertXPathValue(context, "$nan = $num", Boolean.FALSE);
    assertXPathValue(context, "$num = $nan", Boolean.FALSE);
}