// org/apache/commons/compress/ArchiveUtilsTest.java
@Test
public void sanitizeReplacesControlAndSpecials() {
    String input = "a" + '\u0000' + "b" + '\uFFFF' + "c";
    String expected = "a?b?c";
    assertEquals(expected, ArchiveUtils.sanitize(input));
}