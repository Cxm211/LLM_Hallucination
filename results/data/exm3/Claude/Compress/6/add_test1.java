// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testEqualsWithNullName() {
    ZipArchiveEntry entry1 = new ZipArchiveEntry(null);
    ZipArchiveEntry entry2 = new ZipArchiveEntry(null);
    assertTrue(entry1.equals(entry2));
}