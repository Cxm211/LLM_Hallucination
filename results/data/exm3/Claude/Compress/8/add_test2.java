// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalValidWithTrailingSpace() throws Exception {
    byte[] buffer = "123 ".getBytes("UTF-8"); // Valid octal with trailing space
    long result = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(83, result); // 123 octal = 83 decimal
}