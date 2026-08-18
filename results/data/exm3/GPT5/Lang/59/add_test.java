// org/apache/commons/lang/text/StrBuilderAppendInsertTest.java
public void testAppendFixedWidthPadRightNullTruncates() {
        StrBuilder sb = new StrBuilder(1);
        sb.appendFixedWidthPadRight(null, 3, '-');
        assertEquals("nul", sb.toString());
    }