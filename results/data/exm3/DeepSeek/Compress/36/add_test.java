// org/apache/commons/compress/archivers/sevenz/SevenZFileTest.java
@Test
    public void readEmptyEntryMultipleTimes() throws IOException {
        final SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-348.7z"));
        try {
            // skip first entry
            sevenZFile.getNextEntry(); // 1.txt
            SevenZArchiveEntry entry = sevenZFile.getNextEntry(); // 2.txt (empty)
            assertEquals("2.txt", entry.getName());
            // First read should return -1
            assertEquals(-1, sevenZFile.read());
            // Second read should also return -1
            assertEquals(-1, sevenZFile.read());
            // Third read with byte array
            byte[] buf = new byte[10];
            assertEquals(-1, sevenZFile.read(buf));
            assertEquals(-1, sevenZFile.read(buf, 0, 5));
        } finally {
            sevenZFile.close();
        }
    }
