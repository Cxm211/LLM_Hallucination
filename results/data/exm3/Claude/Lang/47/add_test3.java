// org/apache/commons/lang/text/StrBuilderTest.java
public void testAppendFixedWidthPadRightShorterString() {
    StrBuilder sb = new StrBuilder();
    sb.appendFixedWidthPadRight("Hi", 5, '*');
    assertEquals("Hi***", sb.toString());
}