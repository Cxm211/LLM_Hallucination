// org/apache/commons/compress/archivers/sevenz/SevenZFileTest.java
@Test
public void readMultipleEntriesWithEmptyStreams() throws IOException {
    final SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-348.7z"));
    try {
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        while (entry != null) {
            byte[] buffer = new byte[1024];
            int bytesRead = sevenZFile.read(buffer);
            if ("2.txt".equals(entry.getName()) || "5.txt".equals(entry.getName())) {
                assertEquals(-1, bytesRead);
            } else {
                assertTrue(bytesRead >= -1);
            }
            entry = sevenZFile.getNextEntry();
        }
    } finally {
        sevenZFile.close();
    }
}