// org/apache/commons/jxpath/ri/compiler/JXPath149Test.java
public void testComplexOperationWithZeroAndNegative() {
    JXPathContext context = JXPathContext.newContext(null);
    context.getVariables().declareVariable("x", Integer.valueOf(0));
    context.getVariables().declareVariable("y", Integer.valueOf(-1));
    context.getVariables().declareVariable("z", Integer.valueOf(0));
    assertXPathValue(context, "$x + $y <= $z", Boolean.TRUE);
}