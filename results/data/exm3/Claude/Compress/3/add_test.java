// org/apache/commons/compress/archivers/ArchiveOutputStreamTest.java
public void testFinishWithoutEntry() throws Exception {
    OutputStream out1 = new ByteArrayOutputStream();
    ArchiveOutputStream aos1 = factory.createArchiveOutputStream("tar", out1);
    aos1.finish();
}