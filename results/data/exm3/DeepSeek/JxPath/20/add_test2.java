// org/apache/commons/jxpath/ri/compiler/JXPath149Test.java
public void testVariableCollectionRightSide() {
    JXPathContext context = JXPathContext.newContext(null);
    context.getVariables().declareVariable("list", Arrays.asList(1, 3, 5));
    context.getVariables().declareVariable("a", Integer.valueOf(2));
    assertXPathValue(context, "$a <= $list", Boolean.TRUE);
}
