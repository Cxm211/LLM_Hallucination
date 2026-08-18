// org/apache/commons/jxpath/ri/compiler/VariableTest.java
public void testIterateVariableNoMatch() throws Exception {
    assertXPathValue(context, "$d = 'c'", Boolean.FALSE);
}