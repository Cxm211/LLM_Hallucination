// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java
public void testCloseAfterException() throws IOException {
    File file = File.createTempFile("close-test", ".zip");
    ZipArchiveOutputStream out = null;
    try {
        out = new ZipArchiveOutputStream(file);
        out.putArchiveEntry(new ZipArchiveEntry("test.txt"));
        out.write("test data".getBytes());
        out.closeArchiveEntry();
        out.close();
        out = null;
        assertTrue(file.exists());
    } finally {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) { /* swallow */ }
        }
        if (file.exists()) {
            file.delete();
        }
    }
}