// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
    public void testReadingDeflatedEntry() throws Exception {
        File f = File.createTempFile("test", ".zip");
        try (ZipArchiveOutputStream out = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
            entry.setMethod(ZipEntry.DEFLATED);
            out.putArchiveEntry(entry);
            out.write("Hello, world!".getBytes());
            out.closeArchiveEntry();
        }
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(f))) {
            ZipArchiveEntry ze = in.getNextZipEntry();
            assertNotNull(ze);
            assertEquals("test.txt", ze.getName());
            byte[] data = IOUtils.toByteArray(in);
            assertEquals("Hello, world!", new String(data));
        } finally {
            f.delete();
        }
    }
