// org/apache/commons/lang/text/StrBuilderAppendInsertTest.java
public void testAppendFixedWidthPadRight_NullObject() {
    StrBuilder sb = new StrBuilder();
    sb.appendFixedWidthPadRight(null, 6, '-');
    String result = sb.toString();
    assertTrue(result.endsWith("--") || result.endsWith("-"));
    assertEquals(6, result.length());
}