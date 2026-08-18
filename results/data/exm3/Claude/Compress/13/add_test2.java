// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void nullNameHandling() throws Exception {
    ZipArchiveEntry entry = new ZipArchiveEntry("");
    entry.setName(null);
    assertNull(entry.getName());
}