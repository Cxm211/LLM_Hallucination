// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalMultipleTrailingSpacesAndNuls() throws Exception {
    byte[] buffer = new byte[]{'1', '2', '3', ' ', ' ', 0};
    long value = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(0123L, value);
}