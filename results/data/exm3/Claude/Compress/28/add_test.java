// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test(expected = IOException.class)
public void shouldThrowExceptionWhenReadingAfterTruncation() throws Exception {
    TarArchiveInputStream is = getTestStream("/COMPRESS-279.tar");
    try {
        TarArchiveEntry entry = is.getNextTarEntry();
        if (entry != null) {
            byte[] buf = new byte[8192];
            while (is.read(buf) != -1) {
                // Keep reading until truncation is detected
            }
        }
    } finally {
        is.close();
    }
}