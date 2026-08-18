// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java
public void testReadWinZipArchiveMultipleEntriesWithNameChanges() throws IOException, URISyntaxException {
        URL zip = getClass().getResource("/utf8-winzip-test.zip");
        File archive = new File(new URI(zip.toString()));
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, null, true);
            Enumeration<ZipArchiveEntry> entries = zf.getEntries();
            int count = 0;
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                assertNotNull(entry);
                assertNotNull(entry.getName());
                count++;
            }
            assertTrue("Should have multiple entries", count > 1);
            assertCanRead(zf, ASCII_TXT);
            assertCanRead(zf, EURO_FOR_DOLLAR_TXT);
            assertCanRead(zf, OIL_BARREL_TXT);
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }