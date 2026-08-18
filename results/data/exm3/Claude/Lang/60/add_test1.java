// org/apache/commons/lang/text/StrBuilderTest.java
public void testContainsAfterPartialDelete() {
    StrBuilder sb = new StrBuilder("abcdefgh");
    sb.delete(4, 8);
    assertFalse("Should not find character beyond size after delete", sb.contains('e'));
    assertFalse("Should not find character beyond size after delete", sb.contains('h'));
    assertTrue("Should find character within size", sb.contains('d'));
    assertEquals("Should return -1 for character beyond size", -1, sb.indexOf('f'));
    assertEquals("Should return correct index for character within size", 3, sb.indexOf('d'));
}