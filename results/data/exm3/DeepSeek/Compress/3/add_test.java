// org/apache/commons/compress/archivers/ArchiveOutputStreamTest.java
public void testFinishWithDirectoryEntry_Tar() throws Exception {
    OutputStream out = new ByteArrayOutputStream();
    ArchiveOutputStream aos = factory.createArchiveOutputStream("tar", out);
    aos.putArchiveEntry(new TarArchiveEntry("directory/"));
    try {
        aos.finish();
        fail("After putArchive should follow closeArchive");
    } catch (IOException io) {
        // Exception expected
    }
}
