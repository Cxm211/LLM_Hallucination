// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testBothEmptyComments() {
    ZipArchiveEntry entry1 = new ZipArchiveEntry("test");
    ZipArchiveEntry entry2 = new ZipArchiveEntry("test");
    entry1.setComment("");
    entry2.setComment("");
    assertEquals(entry1, entry2);
}