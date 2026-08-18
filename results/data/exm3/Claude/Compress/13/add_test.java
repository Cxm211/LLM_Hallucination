// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void backSlashInMiddleOfName() throws Exception {
    ZipArchiveEntry entry = new ZipArchiveEntry("path\\to\\file.txt");
    assertEquals("path/to/file.txt", entry.getName());
}