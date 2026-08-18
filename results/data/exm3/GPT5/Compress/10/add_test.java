// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testIterateAndReadWinZipArchive
public void testIterateAndReadWinZipArchive() throws IOException, URISyntaxException {
        URL zip = getClass().getResource("/utf8-winzip-test.zip");
        File archive = new File(new URI(zip.toString()));
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, null, true);
            Enumeration<ZipArchiveEntry> en = zf.getEntries();
            while (en.hasMoreElements()) {
                ZipArchiveEntry e = en.nextElement();
                InputStream in = zf.getInputStream(e);
                assertNotNull(in);
                int total = 0;
                byte[] buf = new byte[1024];
                int r;
                while ((r = in.read(buf)) != -1) {
                    total += r;
                }
                in.close();
                assertTrue(total >= 0);
            }
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }