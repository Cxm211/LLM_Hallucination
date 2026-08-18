// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void testParseOctalMultipleTrailingSpacesAndNuls() throws Exception {
    byte[] buffer = new byte[] {'1', '2', '3', ' ', ' ', 0, 0};
    long result = TarUtils.parseOctal(buffer, 0, 7);
    assertEquals(83L, result);
}