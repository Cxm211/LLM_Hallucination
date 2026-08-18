// org/apache/commons/lang/text/StrBuilderTest.java
public void testIndexOfAtEnd() {
    StrBuilder sb = new StrBuilder("abcde");
    assertEquals(3, sb.indexOf("de"));
}
