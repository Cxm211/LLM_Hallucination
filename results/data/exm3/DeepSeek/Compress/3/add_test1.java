// org/apache/commons/compress/archivers/ArchiveOutputStreamTest.java
public void testFinishWithFileWithData_Tar() throws Exception {
    OutputStream out = new ByteArrayOutputStream();
    ArchiveOutputStream aos = factory.createArchiveOutputStream("tar", out);
    TarArchiveEntry entry = new TarArchiveEntry("file.txt");
    entry.setSize(10);
    aos.putArchiveEntry(entry);
    aos.write(new byte[5]);
    try {
        aos.finish();
        fail("After putArchive should follow closeArchive");
    } catch (IOException io) {
        // Exception expected
    }
}
