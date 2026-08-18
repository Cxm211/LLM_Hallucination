// org/apache/commons/jxpath/ri/compiler/JXPath149Test.java::testRightIteratorComparisonOrder
public void testRightIteratorComparisonOrder() {
        JXPathContext context = JXPathContext.newContext(null);
        context.getVariables().declareVariable("list", new int[]{1});
        assertXPathValue(context, "2 < $list", Boolean.FALSE);
    }