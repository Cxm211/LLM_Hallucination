// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void testCentralDirectoryFollowedByInvalidData() throws Exception {
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
    zos.close();
    
    final byte[] zipData = bos.toByteArray();
    final byte[] extendedData = new byte[zipData.length + 4];
    System.arraycopy(zipData, 0, extendedData, 0, zipData.length);
    extendedData[zipData.length] = 0x11;
    extendedData[zipData.length + 1] = 0x22;
    extendedData[zipData.length + 2] = 0x33;
    extendedData[zipData.length + 3] = 0x44;
    
    final ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(extendedData));
    
    assertNotNull(zis.getNextZipEntry());
    assertNull(zis.getNextZipEntry());
    
    zis.close();
}