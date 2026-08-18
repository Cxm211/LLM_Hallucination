// org/apache/commons/lang/text/StrBuilderTest.java
public void testAppendFixedWidthPadLeftShorterString() {
    StrBuilder sb = new StrBuilder();
    sb.appendFixedWidthPadLeft("Hi", 5, '*');
    assertEquals("***Hi", sb.toString());
}