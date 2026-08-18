// org/apache/commons/compress/archivers/cpio/CpioArchiveInputStreamTest.java
public void testMode0EntryNotTrailerThrowsException() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
    CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt");
    entry.setMode(0);
    entry.setSize(0);
    out.putArchiveEntry(entry);
    out.closeArchiveEntry();
    out.finish();
    out.close();
    
    CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
    try {
        in.getNextEntry();
        fail("Expected IOException for mode 0 on non-trailer entry");
    } catch (IOException e) {
        assertTrue(e.getMessage().contains("Mode 0 only allowed in the trailer"));
        assertTrue(e.getMessage().contains("Occurred"));
    } finally {
        in.close();
    }
}