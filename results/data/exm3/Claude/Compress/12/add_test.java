// org/apache/commons/compress/archivers/TarTestCase.java
public void testMalformedHeaderIOException() throws Exception {
    // Create a tar archive with an invalid header that triggers IllegalArgumentException
    byte[] invalidHeader = new byte[512];
    // Fill with invalid magic bytes and other malformed data
    for (int i = 0; i < 512; i++) {
        invalidHeader[i] = (byte) 0xFF;
    }
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(invalidHeader);
    baos.write(new byte[512]); // EOF marker
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("tar", bais);
    try {
        in.getNextEntry();
        fail("Expected IOException");
    } catch (IOException e) {
        Throwable t = e.getCause();
        assertTrue("Expected cause = IllegalArgumentException", t instanceof IllegalArgumentException);
    } finally {
        in.close();
    }
}