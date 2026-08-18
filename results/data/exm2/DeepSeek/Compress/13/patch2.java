    public void testWinzipBackSlashWorkaround() throws Exception {
        URL zip = getClass().getResource("/test-winzip.zip");
        File archive = new File(zip.toURI());
        zf = new ZipFile(archive);
        assertNull(zf.getEntry("ä\\ü.txt"));
        assertNotNull(zf.getEntry("ä/ü.txt"));
    }