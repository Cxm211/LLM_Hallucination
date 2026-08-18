// org/apache/commons/compress/archivers/CpioTestCase.java
public void testCpioCloseIdempotency() throws Exception {
    final File output = new File(dir, "close_test.cpio");
    final File file1 = getFile("test1.xml");
    
    final OutputStream out = new FileOutputStream(output);
    final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("cpio", out);
    os.putArchiveEntry(new CpioArchiveEntry("test1.xml", file1.length()));
    IOUtils.copy(new FileInputStream(file1), os);
    os.closeArchiveEntry();
    
    os.close();
    os.close();
    
    assertTrue("Archive file should exist", output.exists());
    assertTrue("Archive file should have content", output.length() > 0);
}