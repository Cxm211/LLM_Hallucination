// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testNotEqualsNullVsNonNullName() {
        ZipArchiveEntry entryNull = new ZipArchiveEntry(null);
        ZipArchiveEntry entryFoo = new ZipArchiveEntry("foo");
        assertFalse(entryNull.equals(entryFoo));
        assertFalse(entryFoo.equals(entryNull));
    }