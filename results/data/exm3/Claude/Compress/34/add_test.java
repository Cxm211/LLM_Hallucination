// org/apache/commons/compress/archivers/zip/X7875_NewUnixTest.java
@Test
public void testCentralDirectoryLengthIsZero() throws Exception {
    xf = new X7875_NewUnix();
    xf.setUID(1000);
    xf.setGID(1000);
    
    // Local file data should have content
    ZipShort localLength = xf.getLocalFileDataLength();
    assertTrue("Local length should be > 0", localLength.getValue() > 0);
    
    // Central directory data should be empty
    ZipShort centralLength = xf.getCentralDirectoryLength();
    assertEquals("Central directory length should be 0", 0, centralLength.getValue());
}