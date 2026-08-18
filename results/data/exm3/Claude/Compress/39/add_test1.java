// org/apache/commons/compress/ArchiveUtilsTest.java
@Test
public void sanitizeExactly256Chars() {
    StringBuilder input = new StringBuilder();
    for (int i = 0; i < 256; i++) {
        input.append('b');
    }
    String result = ArchiveUtils.sanitize(input.toString());
    assertEquals(255, result.length());
    assertTrue(result.endsWith("..."));
    assertEquals(252, result.length() - 3);
}