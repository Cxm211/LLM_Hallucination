// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void parseOctalWithMinimalLength() throws Exception {
    byte[] buffer = new byte[] {'1', ' '};
    long result = TarUtils.parseOctal(buffer, 0, 2);
    assertEquals(1L, result);
}