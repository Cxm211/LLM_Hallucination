// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void testParseOctalLeadingSpacesThenNull() {
    byte[] buffer = {' ', 0, ' ', ' '};
    long result = TarUtils.parseOctal(buffer, 0, 4);
    assertEquals(0L, result);
}
