// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void canReadEntryDataForUnshrinkingMethod() throws Exception {
    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(ZipMethod.UNSHRINKING.getCode());
        assertTrue(zis.canReadEntryData(e));
    }
}