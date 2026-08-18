// org/apache/commons/jxpath/ri/compiler/VariableTest.java
public void testIterateVariableMultipleMatches() throws Exception {
    assertXPathValue(context, "$d = $d", Boolean.TRUE);
}