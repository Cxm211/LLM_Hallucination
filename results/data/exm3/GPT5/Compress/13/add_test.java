// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void winzipBackSlashWorkaroundAllEntries() throws Exception {
    URL zip = getClass().getResource("/test-winzip.zip");
    ZipArchiveInputStream in = null;
    Set<String> names = new HashSet<String>();
    try {
        in = new ZipArchiveInputStream(new FileInputStream(new File(new URI(zip.toString()))));
        ZipArchiveEntry zae;
        while ((zae = in.getNextZipEntry()) != null) {
            String n = zae.getName();
            names.add(n);
            assertFalse("Entry name should not contain backslash: " + n, n.indexOf('\\') >= 0);
        }
    } finally {
        if (in != null) {
            in.close();
        }
    }
    assertTrue(names.contains("\u00e4/\u00fc.txt"));
}