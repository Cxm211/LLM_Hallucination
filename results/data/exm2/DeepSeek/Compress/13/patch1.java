@Test
    public void winzipBackSlashWorkaround() throws Exception {
        URL zip = getClass().getResource("/test-winzip.zip");
        ZipArchiveInputStream in = null;
        try {
            in = new ZipArchiveInputStream(new FileInputStream(new File(zip.toURI())));
            ZipArchiveEntry zae = in.getNextZipEntry();
            zae = in.getNextZipEntry();
            zae = in.getNextZipEntry();
            assertEquals("ä/", zae.getName());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }