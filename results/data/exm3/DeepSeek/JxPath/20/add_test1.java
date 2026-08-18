// org/apache/commons/jxpath/ri/compiler/JXPath149Test.java
public void testVariableCollectionComparison() {
    JXPathContext context = JXPathContext.newContext(null);
    context.getVariables().declareVariable("list", Arrays.asList(1, 2, 3));
    assertXPathValue(context, "$list <= 2", Boolean.TRUE);
}
