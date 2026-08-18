// org/apache/commons/lang/text/StrBuilderAppendInsertTest.java
public void testAppendFixedWidthPadRight_PadShorterString() {
    StrBuilder sb = new StrBuilder();
    sb.appendFixedWidthPadRight("hi", 5, '*');
    assertEquals("hi***", sb.toString());
}