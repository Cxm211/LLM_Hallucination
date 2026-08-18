// org/apache/commons/lang/text/StrBuilderTest.java
public void testIndexOfExactMatch() {
    StrBuilder sb = new StrBuilder("test");
    assertEquals(0, sb.indexOf("test", 0));
    sb.deleteFirst("test");
    assertEquals(-1, sb.indexOf("test", 0));
}