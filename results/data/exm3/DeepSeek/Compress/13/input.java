// buggy function
    protected void setName(String name) {
        this.name = name;
    }

// trigger testcase
// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java::winzipBackSlashWorkaround
@Test
    public void winzipBackSlashWorkaround() throws Exception {
        URL zip = getClass().getResource("/test-winzip.zip");
        ZipArchiveInputStream in = null;
        try {
            in = new ZipArchiveInputStream(new FileInputStream(new File(new URI(zip.toString()))));
            ZipArchiveEntry zae = in.getNextZipEntry();
            zae = in.getNextZipEntry();
            zae = in.getNextZipEntry();
            assertEquals("\u00e4/", zae.getName());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

// org/apache/commons/compress/archivers/zip/ZipFileTest.java::testWinzipBackSlashWorkaround
public void testWinzipBackSlashWorkaround() throws Exception {
        URL zip = getClass().getResource("/test-winzip.zip");
        File archive = new File(new URI(zip.toString()));
        zf = new ZipFile(archive);
        assertNull(zf.getEntry("\u00e4\\\u00fc.txt"));
        assertNotNull(zf.getEntry("\u00e4/\u00fc.txt"));
    }
