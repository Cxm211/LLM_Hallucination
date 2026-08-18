// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void testParseOctalMultipleTrailingSpaces() {
    byte[] buffer = {'1','2','3',' ',' ',' '};
    long result = TarUtils.parseOctal(buffer, 0, 6);
    assertEquals(83L, result); // octal 123 = decimal 83
}
