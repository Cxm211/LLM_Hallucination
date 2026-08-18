// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java::testNullCommentEqualsEmptyCommentSymmetry
public void testNullCommentEqualsEmptyCommentSymmetry() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        entry1.setComment(null);
        entry2.setComment("");
        assertEquals(entry2, entry1);
    }