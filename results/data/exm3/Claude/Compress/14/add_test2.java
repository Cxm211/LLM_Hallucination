// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void parseOctalWithLeadingSpacesAndMinimal() throws Exception {
    byte[] buffer = new byte[] {' ', ' ', '7', 0};
    long result = TarUtils.parseOctal(buffer, 0, 4);
    assertEquals(7L, result);
}