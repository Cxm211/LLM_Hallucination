// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testEmptyNodeSetWithString() {
    assertXPathValue(context, "/idonotexist = 'test'", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "/idonotexist != 'test'", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$array[position() < 1] = 'value'", Boolean.FALSE, Boolean.class);
}