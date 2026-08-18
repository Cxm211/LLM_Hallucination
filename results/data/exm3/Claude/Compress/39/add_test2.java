// org/apache/commons/compress/ArchiveUtilsTest.java
@Test
public void sanitizeWithControlCharactersAndShortening() {
    StringBuilder input = new StringBuilder();
    for (int i = 0; i < 260; i++) {
        if (i % 10 == 0) {
            input.append('\u0000');
        } else {
            input.append('x');
        }
    }
    String result = ArchiveUtils.sanitize(input.toString());
    assertEquals(255, result.length());
    assertTrue(result.endsWith("..."));
    for (int i = 0; i < 252; i++) {
        char c = result.charAt(i);
        assertTrue(c == '?' || c == 'x');
    }
}