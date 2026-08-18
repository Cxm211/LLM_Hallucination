// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java::shouldThrowAnExceptionOnTruncatedEntriesWhenReadingManually
public void shouldThrowAnExceptionOnTruncatedEntriesWhenReadingManually() throws Exception {
    TarArchiveInputStream is = getTestStream("/COMPRESS-279.tar");
    try {
        TarArchiveEntry entry = is.getNextTarEntry();
        assertNotNull(entry);
        byte[] buf = new byte[512];
        try {
            while (true) {
                // read in chunks; truncated entry should trigger IOException
                int r = is.read(buf, 0, buf.length);
                if (r == -1) {
                    // if we reached -1 without an exception, the implementation is buggy
                    fail("Expected IOException for truncated entry, but reached EOF");
                }
            }
        } catch (IOException expected) {
            // expected
        }
    } finally {
        is.close();
    }
}