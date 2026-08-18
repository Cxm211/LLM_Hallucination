// org/apache/commons/lang/text/StrBuilderTest.java
public void testAppendFixedWidthPadLeftLongerString() {
    StrBuilder sb = new StrBuilder();
    sb.appendFixedWidthPadLeft("HelloWorld", 5, '*');
    assertEquals("World", sb.toString());
}