// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java
public void testZipFileDoubleClose() throws IOException {
    File file = File.createTempFile("zip-double-close", ".zip");
    // Create a simple zip file
    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new FileOutputStream(file))) {
        zos.putArchiveEntry(new ZipArchiveEntry("test.txt"));
        zos.write("Hello".getBytes());
        zos.closeArchiveEntry();
    }
    ZipFile zf = null;
    try {
        zf = new ZipFile(file);
        // read something
        assertNotNull(zf.getEntry("test.txt"));
        zf.close(); // first close
        zf.close(); // second close should not throw
    } finally {
        ZipFile.closeQuietly(zf);
        if (file.exists()) {
            file.delete();
        }
    }
}
