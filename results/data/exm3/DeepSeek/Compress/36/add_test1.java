// org/apache/commons/compress/archivers/sevenz/SevenZFileTest.java
@Test
    public void readEmptyThenNonEmpty() throws IOException {
        final SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-348.7z"));
        try {
            // skip first entry
            sevenZFile.getNextEntry(); // 1.txt
            SevenZArchiveEntry emptyEntry = sevenZFile.getNextEntry(); // 2.txt (empty)
            assertEquals("2.txt", emptyEntry.getName());
            // read from empty entry should return -1
            assertEquals(-1, sevenZFile.read());
            // now move to next entry (3.txt) which is non-empty
            SevenZArchiveEntry nextEntry = sevenZFile.getNextEntry();
            assertNotNull(nextEntry);
            assertNotEquals("2.txt", nextEntry.getName());
            // read first byte from non-empty entry should be valid
            int b = sevenZFile.read();
            assertNotEquals(-1, b);
        } finally {
            sevenZFile.close();
        }
    }
