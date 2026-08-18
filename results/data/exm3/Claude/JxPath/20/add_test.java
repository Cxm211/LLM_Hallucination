// org/apache/commons/jxpath/ri/compiler/JXPath149Test.java
public void testNaNComparison() {
    JXPathContext context = JXPathContext.newContext(null);
    context.getVariables().declareVariable("nan1", Double.NaN);
    context.getVariables().declareVariable("nan2", Double.NaN);
    assertXPathValue(context, "$nan1 = $nan2", Boolean.TRUE);
}