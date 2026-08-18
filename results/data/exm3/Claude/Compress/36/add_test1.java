// org/apache/commons/compress/archivers/sevenz/SevenZFileTest.java
@Test
public void skipEntriesOfSize0() throws IOException {
    final SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-348.7z"));
    try {
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        int count = 0;
        while (entry != null) {
            count++;
            if ("2.txt".equals(entry.getName()) || "5.txt".equals(entry.getName())) {
                long skipped = sevenZFile.skip(100);
                assertEquals(0, skipped);
            }
            entry = sevenZFile.getNextEntry();
        }
        assertEquals(5, count);
    } finally {
        sevenZFile.close();
    }
}