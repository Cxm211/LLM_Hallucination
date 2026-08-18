// org/apache/commons/lang/text/StrBuilderTest.java
public void testAppendFixedWidthPadLeftZeroWidth() {
    StrBuilder sb = new StrBuilder();
    sb.append("Test");
    sb.appendFixedWidthPadLeft("Hello", 0, '*');
    assertEquals("Test", sb.toString());
}