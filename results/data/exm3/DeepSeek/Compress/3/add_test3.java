// org/apache/commons/compress/archivers/ArchiveOutputStreamTest.java
public void testFinishWithLongNameTruncate_Tar() throws Exception {
    OutputStream out = new ByteArrayOutputStream();
    ArchiveOutputStream aos = factory.createArchiveOutputStream("tar", out);
    TarArchiveOutputStream taos = (TarArchiveOutputStream) aos;
    taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
    String longName = new String(new char[100]).replace('\u0000', 'a');
    aos.putArchiveEntry(new TarArchiveEntry(longName));
    try {
        aos.finish();
        fail("After putArchive should follow closeArchive");
    } catch (IOException io) {
        // Exception expected
    }
}
