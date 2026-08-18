// org/apache/commons/compress/ArchiveUtilsTest.java
@Test
public void sanitizeExactly255Chars() {
    StringBuilder input = new StringBuilder();
    for (int i = 0; i < 255; i++) {
        input.append('a');
    }
    String result = ArchiveUtils.sanitize(input.toString());
    assertEquals(255, result.length());
    assertEquals(input.toString(), result);
}