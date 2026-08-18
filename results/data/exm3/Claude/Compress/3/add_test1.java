// org/apache/commons/compress/archivers/ArchiveOutputStreamTest.java
public void testFinishAfterCloseEntry() throws Exception {
    OutputStream out1 = new ByteArrayOutputStream();
    ArchiveOutputStream aos1 = factory.createArchiveOutputStream("tar", out1);
    aos1.putArchiveEntry(new TarArchiveEntry("dummy"));
    aos1.closeArchiveEntry();
    aos1.finish();
}