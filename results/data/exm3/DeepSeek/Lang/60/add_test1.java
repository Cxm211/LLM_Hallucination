// org/apache/commons/lang/text/StrBuilderTest.java
public void testContainsIndexOfBeyondSize() {
    StrBuilder sb = new StrBuilder("abc");
    sb.append("d");
    sb.delete(3, 4);
    assertFalse("contains should not find char beyond size", sb.contains('d'));
    assertEquals("indexOf should return -1 for char beyond size", -1, sb.indexOf('d'));
    assertEquals("indexOf with startIndex 1 should return -1 for char beyond size", -1, sb.indexOf('d', 1));
}
