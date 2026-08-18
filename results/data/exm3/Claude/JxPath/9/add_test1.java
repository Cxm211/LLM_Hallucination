// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNanNotEqualToItself() {
    assertXPathValue(context, "$nan != $nan", Boolean.TRUE, Boolean.class);
}