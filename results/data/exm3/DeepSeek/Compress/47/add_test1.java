// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
    public void testCanReadEntryDataDeflatedWithDataDescriptor() throws Exception {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(ZipEntry.DEFLATED);
        e.getGeneralPurposeBit().useDataDescriptor(true);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), false)) {
            assertTrue(zis.canReadEntryData(e));
        }
    }
