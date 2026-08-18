// org/apache/commons/compress/archivers/zip/ZipFileTest.java
public void testBackSlashAtStart() throws Exception {
    ZipArchiveEntry entry = new ZipArchiveEntry("\\startfile.txt");
    assertEquals("/startfile.txt", entry.getName());
}