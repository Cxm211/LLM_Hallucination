// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
public void testTruncatedEntryImmediateEOF() throws Exception {
        TarArchiveInputStream is = getTestStream("/COMPRESS-279.tar");
        TarArchiveEntry entry = is.getNextTarEntry();
        assertNotNull(entry);
        byte[] buf = new byte[1024];
        try {
            int read = is.read(buf, 0, 1024);
            fail("Expected IOException for truncated entry");
        } catch (IOException e) {
            // expected
        } finally {
            is.close();
        }
    }
