// org/apache/commons/compress/archivers/zip/ZipFileTest.java
@Test
    public void testBackslashNormalizationInZipFile() throws Exception {
        File tmp = File.createTempFile("test", ".zip");
        try {
            ZipArchiveOutputStream out = new ZipArchiveOutputStream(tmp);
            out.putArchiveEntry(new ZipArchiveEntry("foo\\bar.txt"));
            out.write("content".getBytes());
            out.closeArchiveEntry();
            out.finish();
            out.close();
            
            ZipFile zf = new ZipFile(tmp);
            try {
                assertNull(zf.getEntry("foo\\bar.txt"));
                assertNotNull(zf.getEntry("foo/bar.txt"));
                ZipArchiveEntry entry = zf.getEntry("foo/bar.txt");
                assertEquals("foo/bar.txt", entry.getName());
            } finally {
                zf.close();
            }
        } finally {
            tmp.delete();
        }
    }
