// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java
public void testReadWinZipArchiveWithData() throws IOException, URISyntaxException {
        URL zip = getClass().getResource("/utf8-winzip-test.zip");
        File archive = new File(new URI(zip.toString()));
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, null, true);
            Enumeration<ZipArchiveEntry> entries = zf.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                try (InputStream is = zf.getInputStream(entry)) {
                    byte[] buffer = new byte[1024];
                    while (is.read(buffer) != -1) {
                        // consume data
                    }
                }
            }
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }
