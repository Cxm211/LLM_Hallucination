// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void testParseOctalThreeTrailingSpaces() throws Exception {
    byte[] buffer = new byte[] {'7', '7', ' ', ' ', ' '};
    long result = TarUtils.parseOctal(buffer, 0, 5);
    assertEquals(63L, result);
}