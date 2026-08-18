// org/apache/commons/lang/text/StrBuilderTest.java
public void testAppendFixedWidthPadRightLongerString() {
    StrBuilder sb = new StrBuilder();
    sb.appendFixedWidthPadRight("HelloWorld", 5, '*');
    assertEquals("Hello", sb.toString());
}