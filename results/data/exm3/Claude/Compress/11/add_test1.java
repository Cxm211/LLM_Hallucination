// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java
@Test
public void smallBinaryDataIsNoArchive() throws Exception {
    try {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        new ArchiveStreamFactory()
            .createArchiveInputStream(new ByteArrayInputStream(data));
        fail("created an input stream for non-archive binary data");
    } catch (ArchiveException ae) {
        assertTrue(ae.getMessage().startsWith("No Archiver found"));
    }
}