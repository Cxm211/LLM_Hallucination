// org/apache/commons/compress/archivers/cpio/CpioArchiveInputStreamTest.java
public void testMode0TrailerDoesNotThrowException() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
    CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file.txt");
    entry.setSize(0);
    out.putArchiveEntry(entry);
    out.closeArchiveEntry();
    out.finish();
    out.close();
    
    CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
    CpioArchiveEntry readEntry = in.getNextEntry();
    assertNotNull(readEntry);
    assertEquals("file.txt", readEntry.getName());
    readEntry = in.getNextEntry();
    assertNull(readEntry);
    in.close();
}