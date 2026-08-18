// org/apache/commons/compress/ArchiveUtilsTest.java
@Test
public void sanitizeOnlyControlCharacters() {
    String input = "\u0000\u0001\u0002\u0003\u0004";
    String result = ArchiveUtils.sanitize(input);
    assertEquals("?????", result);
}