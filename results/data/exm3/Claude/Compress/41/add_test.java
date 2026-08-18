// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void testInvalidSignatureAfterValidEntry() throws Exception {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
    
    final ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
    entry.setMethod(ZipArchiveOutputStream.STORED);
    entry.setSize(5);
    entry.setCrc(0x8587d865L);
    zos.putArchiveEntry(entry);
    zos.write("hello".getBytes());
    zos.closeArchiveEntry();
    zos.finish();
    
    final byte[] zipData = bos.toByteArray();
    final byte[] corruptedData = new byte[zipData.length + 30];
    System.arraycopy(zipData, 0, corruptedData, 0, zipData.length);
    corruptedData[zipData.length] = 0x50;
    corruptedData[zipData.length + 1] = 0x4b;
    corruptedData[zipData.length + 2] = 0x99;
    corruptedData[zipData.length + 3] = 0x99;
    
    final ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(corruptedData));
    
    assertNotNull(zis.getNextZipEntry());
    
    try {
        zis.getNextZipEntry();
        fail("ZipException expected");
    } catch (ZipException expected) {
        assertTrue(expected.getMessage().contains("Unexpected record signature"));
    } finally {
        zis.close();
    }
}