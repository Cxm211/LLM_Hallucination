// org/apache/commons/compress/archivers/zip/X7875_NewUnixTest.java
@Test
public void testLengthsAfterParsing() throws Exception {
    xf = new X7875_NewUnix();
    
    // Version=1, Len=2, one thousand, Len=2, one thousand
    final byte[] ONE_THOUSAND_UID_GID = {1, 2, -24, 3, 2, -24, 3};
    
    xf.parseFromLocalFileData(ONE_THOUSAND_UID_GID, 0, ONE_THOUSAND_UID_GID.length);
    
    assertEquals(1000, xf.getUID());
    assertEquals(1000, xf.getGID());
    
    // After parsing, local length should match the data
    ZipShort localLength = xf.getLocalFileDataLength();
    assertEquals(ONE_THOUSAND_UID_GID.length, localLength.getValue());
    
    // Central directory should still be 0
    ZipShort centralLength = xf.getCentralDirectoryLength();
    assertEquals(0, centralLength.getValue());
}