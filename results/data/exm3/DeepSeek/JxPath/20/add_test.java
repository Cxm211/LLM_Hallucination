// org/apache/commons/jxpath/ri/compiler/JXPath149Test.java
public void testVariableComparison() {
    JXPathContext context = JXPathContext.newContext(null);
    context.getVariables().declareVariable("x", Integer.valueOf(1));
    context.getVariables().declareVariable("y", Integer.valueOf(2));
    assertXPathValue(context, "$x <= $y", Boolean.TRUE);
}
