// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
    public void testCanReadEntryDataStoredWithDataDescriptor() throws Exception {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(ZipEntry.STORED);
        e.getGeneralPurposeBit().useDataDescriptor(true);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), true)) {
            assertTrue(zis.canReadEntryData(e));
        }
    }
