// org/apache/commons/compress/archivers/jar/JarArchiveOutputStreamTest.java
public void testCloseWithFinishException() throws IOException {
    File testArchive = File.createTempFile("jar-close-test", ".jar");
    JarArchiveOutputStream out = null;
    try {
        out = new JarArchiveOutputStream(new FileOutputStream(testArchive));
        out.putArchiveEntry(new ZipArchiveEntry("test/"));
        out.closeArchiveEntry();
        // Do not call finish(), close should still work
        out.close();
        out = null;
        assertTrue(testArchive.exists());
    } finally {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) { /* swallow */ }
        }
        if (testArchive.exists()) {
            testArchive.delete();
        }
    }
}