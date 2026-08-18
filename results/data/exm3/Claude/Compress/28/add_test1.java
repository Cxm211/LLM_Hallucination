// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test(expected = IOException.class)
public void shouldThrowExceptionOnPartialRead() throws Exception {
    TarArchiveInputStream is = getTestStream("/COMPRESS-279.tar");
    try {
        TarArchiveEntry entry = is.getNextTarEntry();
        if (entry != null) {
            byte[] buf = new byte[1];
            while (is.read(buf, 0, 1) != -1) {
                // Read byte by byte to detect truncation
            }
        }
    } finally {
        is.close();
    }
}