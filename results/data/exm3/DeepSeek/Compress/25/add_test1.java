// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
    public void testReadingStoredEntryWithoutDataDescriptor() throws Exception {
        File f = File.createTempFile("test", ".zip");
        try (ZipArchiveOutputStream out = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
            entry.setMethod(ZipEntry.STORED);
            byte[] content = "data".getBytes();
            entry.setSize(content.length);
            CRC32 crc = new CRC32();
            crc.update(content);
            entry.setCrc(crc.getValue());
            out.putArchiveEntry(entry);
            out.write(content);
            out.closeArchiveEntry();
        }
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(f))) {
            ZipArchiveEntry ze = in.getNextZipEntry();
            assertNotNull(ze);
            assertEquals("test.txt", ze.getName());
            byte[] data = IOUtils.toByteArray(in);
            assertEquals("data", new String(data));
        } finally {
            f.delete();
        }
    }
