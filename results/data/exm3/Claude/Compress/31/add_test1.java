// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
@Test
public void testParseOctalTrailingNulsOnly() throws Exception {
    byte[] buffer = new byte[]{0, 0, 0, 0}; // All NULs
    long result = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(0L, result);
}