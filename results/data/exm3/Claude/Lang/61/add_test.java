// org/apache/commons/lang/text/StrBuilderTest.java
public void testIndexOfAtEnd() {
    StrBuilder sb = new StrBuilder("abcdefgh");
    assertEquals(5, sb.indexOf("fgh", 0));
    assertEquals(5, sb.indexOf("fgh", 5));
    assertEquals(-1, sb.indexOf("fgh", 6));
}