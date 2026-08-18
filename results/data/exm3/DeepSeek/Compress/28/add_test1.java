// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
public void testPartialReadThenTruncation() throws Exception {
        TarArchiveInputStream is = getTestStream("/COMPRESS-279.tar");
        TarArchiveEntry entry = is.getNextTarEntry();
        assertNotNull(entry);
        byte[] buf = new byte[512];
        int firstRead = is.read(buf, 0, 512);
        if (firstRead > 0) {
            try {
                int secondRead = is.read(buf, 0, 512);
                fail("Expected IOException for truncated entry after partial read");
            } catch (IOException e) {
                // expected
            }
        }
        is.close();
    }
