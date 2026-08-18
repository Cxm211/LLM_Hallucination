// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalValidWithTrailingNul() throws Exception {
    byte[] buffer = new byte[]{0x31, 0x32, 0x33, 0x00}; // "123\0"
    long result = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(83, result); // 123 octal = 83 decimal
}