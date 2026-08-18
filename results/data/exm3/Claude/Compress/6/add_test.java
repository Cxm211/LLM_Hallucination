// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testEqualsWithSameName() {
    ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
    ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
    assertTrue(entry1.equals(entry2));
}