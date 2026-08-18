// org/apache/commons/lang/text/StrBuilderAppendInsertTest.java
public void testAppendFixedWidthPadRight_TruncateLongerString() {
    StrBuilder sb = new StrBuilder();
    sb.appendFixedWidthPadRight("hello", 3, '-');
    assertEquals("hel", sb.toString());
}