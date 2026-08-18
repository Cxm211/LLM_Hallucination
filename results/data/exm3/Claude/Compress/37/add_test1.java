// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void parsePaxHeaderWithMultipleBlankLines() throws Exception {
    final TarArchiveInputStream is = getTestStream("/COMPRESS-355-multiple-blanks.tar");
    try {
        final TarArchiveEntry entry = is.getNextTarEntry();
        assertNotNull(entry);
        assertNull(is.getNextTarEntry());
    } finally {
        is.close();
    }
}