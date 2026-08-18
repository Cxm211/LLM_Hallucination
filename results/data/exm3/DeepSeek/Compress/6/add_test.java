// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testEqualsDifferentSize() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        entry1.setSize(100);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        entry2.setSize(200);
        assertFalse(entry1.equals(entry2));
    }
