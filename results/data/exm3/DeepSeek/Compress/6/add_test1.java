// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
public void testHashCodeConsistentWithEquals() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        entry1.setSize(100);
        entry1.setCrc(1234);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        entry2.setSize(100);
        entry2.setCrc(1234);
        assertTrue(entry1.equals(entry2));
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }
