// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void parseOctalWithOnlyTrailers() throws Exception {
    byte[] buffer = new byte[] {' ', ' ', 0};
    long result = TarUtils.parseOctal(buffer, 0, 3);
    assertEquals(0L, result);
}