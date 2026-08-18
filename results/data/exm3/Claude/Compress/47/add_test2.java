// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void canReadEntryDataForBzip2Method() throws Exception {
    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(ZipMethod.BZIP2.getCode());
        assertTrue(zis.canReadEntryData(e));
    }
}