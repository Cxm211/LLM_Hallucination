// org/apache/commons/compress/archivers/zip/Maven221MultiVolumeTest.java::testRead7ZipMultiVolumeArchiveForStream
public void testRead7ZipMultiVolumeArchiveForStreamSingleByte() throws IOException, URISyntaxException {
    URL zip = getClass().getResource("/apache-maven-2.2.1.zip.001");
    FileInputStream archive = new FileInputStream(new File(new URI(zip.toString())));
    ZipArchiveInputStream zi = null;
    try {
        zi = new ZipArchiveInputStream(archive, null, false);
        for (int i = 0; i < ENTRIES.length; i++) {
            assertEquals(ENTRIES[i], zi.getNextEntry().getName());
        }
        ArchiveEntry lastEntry = zi.getNextEntry();
        assertEquals(LAST_ENTRY_NAME, lastEntry.getName());
        byte[] buffer = new byte[1];
        try {
            zi.read(buffer);
            fail("shouldn't be able to read from truncated entry (single byte)");
        } catch (IOException e) {
            assertEquals("Truncated ZIP file", e.getMessage());
        }
    } finally {
        if (zi != null) {
            zi.close();
        }
    }
}