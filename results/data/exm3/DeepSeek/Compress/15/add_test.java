// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testEmptyCommentEqualsNullComment() {
    ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
    ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
    entry1.setComment("");
    entry2.setComment(null);
    assertEquals(entry1, entry2);
    assertEquals(entry2, entry1);
}
