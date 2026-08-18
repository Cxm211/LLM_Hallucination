// org/apache/commons/compress/ArchiveUtilsTest.java
@Test
public void sanitizeEmptyString() {
    String result = ArchiveUtils.sanitize("");
    assertEquals("", result);
}