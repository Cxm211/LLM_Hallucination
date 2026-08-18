// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java
@Test
public void exactly512BytesOfNonTarDataIsNoArchive() throws Exception {
    try {
        byte[] data = new byte[512];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        new ArchiveStreamFactory()
            .createArchiveInputStream(new ByteArrayInputStream(data));
        fail("created an input stream for 512 bytes of non-tar data");
    } catch (ArchiveException ae) {
        assertTrue(ae.getMessage().startsWith("No Archiver found"));
    }
}