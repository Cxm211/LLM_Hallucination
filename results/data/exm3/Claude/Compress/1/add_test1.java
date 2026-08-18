// org/apache/commons/compress/archivers/CpioTestCase.java
public void testCpioCloseWithException() throws Exception {
    final File output = new File(dir, "exception_test.cpio");
    final File file1 = getFile("test1.xml");
    
    final OutputStream out = new FileOutputStream(output);
    final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("cpio", out);
    os.putArchiveEntry(new CpioArchiveEntry("test1.xml", file1.length()));
    IOUtils.copy(new FileInputStream(file1), os);
    os.closeArchiveEntry();
    
    out.close();
    
    try {
        os.close();
        fail("Expected IOException when closing after underlying stream is closed");
    } catch (IOException e) {
    }
    
    final InputStream is = new FileInputStream(output);
    final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("cpio", is);
    ArchiveEntry entry = in.getNextEntry();
    assertNotNull("Should be able to read entry", entry);
    in.close();
}