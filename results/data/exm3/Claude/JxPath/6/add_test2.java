// org/apache/commons/jxpath/ri/compiler/VariableTest.java
public void testIterateVariableSecondElement() throws Exception {
    assertXPathValue(context, "'b' = $d", Boolean.TRUE);
}