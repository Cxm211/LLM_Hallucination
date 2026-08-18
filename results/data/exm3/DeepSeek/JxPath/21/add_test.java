// org/apache/commons/jxpath/ri/model/MixedModelTest.java
public void testGetLengthForNull() {
    JXPathContext ctx = JXPathContext.newContext(new TestNull());
    assertXPathValue(ctx, "count(nothing)", Integer.valueOf(0));
    assertXPathValue(ctx, "count(child/nothing)", Integer.valueOf(0));
    assertXPathValue(ctx, "count(array[2])", Integer.valueOf(0));
}
