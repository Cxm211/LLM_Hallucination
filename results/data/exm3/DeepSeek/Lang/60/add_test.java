// org/apache/commons/lang/text/StrBuilderTest.java
public void testContainsIndexOfAfterClear() {
    StrBuilder sb = new StrBuilder("abc");
    sb.delete(0, sb.length());
    assertFalse("contains should return false after clear", sb.contains('a'));
    assertEquals("indexOf should return -1 after clear", -1, sb.indexOf('a'));
    assertEquals("indexOf with startIndex 0 should return -1 after clear", -1, sb.indexOf('a', 0));
}
