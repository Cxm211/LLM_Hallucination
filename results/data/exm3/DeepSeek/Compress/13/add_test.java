// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
    public void testBackslashNormalization() throws Exception {
        File tmp = File.createTempFile("test", ".zip");
        try {
            ZipArchiveOutputStream out = new ZipArchiveOutputStream(tmp);
            out.putArchiveEntry(new ZipArchiveEntry("a\\b.txt"));
            out.write("content".getBytes());
            out.closeArchiveEntry();
            out.putArchiveEntry(new ZipArchiveEntry("dir\\"));
            out.closeArchiveEntry();
            out.putArchiveEntry(new ZipArchiveEntry("x\\y\\z.txt"));
            out.write("another".getBytes());
            out.closeArchiveEntry();
            out.finish();
            out.close();
            
            ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(tmp));
            ZipArchiveEntry e;
            List<String> names = new ArrayList<>();
            while ((e = in.getNextZipEntry()) != null) {
                names.add(e.getName());
            }
            in.close();
            
            assertEquals(Arrays.asList("a/b.txt", "dir/", "x/y/z.txt"), names);
        } finally {
            tmp.delete();
        }
    }
