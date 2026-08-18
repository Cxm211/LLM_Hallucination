// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java
@Test
public void testShortStreamLessThan512Bytes() throws Exception {
    InputStream is = null;
    try {
        // Create a stream with less than 512 bytes that doesn't match any archive format
        byte[] shortData = new byte[256];
        for (int i = 0; i < shortData.length; i++) {
            shortData[i] = (byte) (i % 256);
        }
        is = new BufferedInputStream(new ByteArrayInputStream(shortData));
        try {
            new ArchiveStreamFactory().createArchiveInputStream(is);
            fail("created an input stream for a non-archive");
        } catch (ArchiveException ae) {
            assertTrue(ae.getMessage().startsWith("No Archiver found"));
        }
    } finally {
        if (is != null) {
            is.close();
        }
    }
}