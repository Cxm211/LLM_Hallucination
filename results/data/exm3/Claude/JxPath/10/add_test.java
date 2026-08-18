// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testEmptyNodeSetEqualityWithNonZero() {
    assertXPathValue(context, "/idonotexist = 1", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "/idonotexist != 1", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$array[position() < 1] = 5", Boolean.FALSE, Boolean.class);
    assertXPathValue(context, "$array[position() < 1] != 5", Boolean.FALSE, Boolean.class);
}