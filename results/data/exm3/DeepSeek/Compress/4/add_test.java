// org/apache/commons/compress/archivers/jar/JarArchiveOutputStreamTest.java
public void testDoubleClose() throws IOException {
    File testArchive = File.createTempFile("jar-double-close", ".jar");
    JarArchiveOutputStream out = null;
    try {
        out = new JarArchiveOutputStream(new FileOutputStream(testArchive));
        out.putArchiveEntry(new ZipArchiveEntry("test.txt"));
        out.write("Hello".getBytes());
        out.closeArchiveEntry();
        out.close(); // first close
        out.close(); // second close should not throw
    } finally {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) { /* ignore */ }
        }
        if (testArchive.exists()) {
            testArchive.delete();
        }
    }
}
