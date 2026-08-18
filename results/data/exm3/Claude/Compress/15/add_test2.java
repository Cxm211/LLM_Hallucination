// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testNonEmptyCommentNotEqualsEmpty() {
    ZipArchiveEntry entry1 = new ZipArchiveEntry("test");
    ZipArchiveEntry entry2 = new ZipArchiveEntry("test");
    entry1.setComment("comment");
    entry2.setComment("");
    assertFalse(entry1.equals(entry2));
}