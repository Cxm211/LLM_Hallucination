// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void testThrowOnInvalidEntrySynthetic() throws Exception {
    byte[] header = new byte[30];
    header[0] = 0x78; // invalid signature 0x12345678 in little-endian
    header[1] = 0x56;
    header[2] = 0x34;
    header[3] = 0x12;
    ByteArrayInputStream bais = new ByteArrayInputStream(header);
    ZipArchiveInputStream zip = new ZipArchiveInputStream(bais);
    try {
        zip.getNextZipEntry();
        fail("IOException expected");
    } catch (ZipException expected) {
        assertTrue(expected.getMessage().contains("Unexpected record signature"));
    } finally {
        zip.close();
    }
}