// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testNotEqualsOneNull() {
    ZipArchiveEntry entry1 = new ZipArchiveEntry(null);
    ZipArchiveEntry entry2 = new ZipArchiveEntry("bar");
    assertFalse(entry1.equals(entry2));
}