// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void testParseOctalAlternatingTrailingNulsAndSpaces() throws Exception {
    byte[] buffer = new byte[] {'5', '4', '3', 0, ' ', 0};
    long result = TarUtils.parseOctal(buffer, 0, 6);
    assertEquals(355L, result);
}