// org/apache/commons/lang/text/StrBuilderAppendInsertTest.java
public void testAppendFixedWidthPadRight_ExactWidth() {
    StrBuilder sb = new StrBuilder();
    sb.appendFixedWidthPadRight("test", 4, '-');
    assertEquals("test", sb.toString());
}