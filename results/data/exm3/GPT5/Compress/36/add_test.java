// org/apache/commons/compress/archivers/sevenz/SevenZFileTest.java::readEntriesOfSize0BulkRead
@Test
public void readEntriesOfSize0BulkRead() throws IOException {
    final SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-348.7z"));
    try {
        int entries = 0;
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        byte[] buf = new byte[16];
        while (entry != null) {
            entries++;
            int n = sevenZFile.read(buf);
            if ("2.txt".equals(entry.getName()) || "5.txt".equals(entry.getName())) {
                assertEquals(-1, n);
            } else {
                assertNotEquals(-1, n);
            }
            entry = sevenZFile.getNextEntry();
        }
        assertEquals(5, entries);
    } finally {
        sevenZFile.close();
    }
}