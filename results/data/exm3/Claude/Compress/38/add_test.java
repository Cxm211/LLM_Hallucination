// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void paxHeaderNotConsideredDirectory() throws Exception {
    final TarArchiveInputStream is = getTestStream("/COMPRESS-356.tar");
    try {
        final TarArchiveEntry entry = is.getNextTarEntry();
        assertEquals("package/package.json", entry.getName());
        assertFalse("Pax header should not be considered a directory", entry.isDirectory());
        assertNull(is.getNextTarEntry());
    } finally {
        is.close();
    }
}