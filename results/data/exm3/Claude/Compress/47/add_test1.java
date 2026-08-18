// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void canReadEntryDataForImplodingMethod() throws Exception {
    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(ZipMethod.IMPLODING.getCode());
        assertTrue(zis.canReadEntryData(e));
    }
}