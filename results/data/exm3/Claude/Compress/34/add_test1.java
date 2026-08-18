// org/apache/commons/compress/archivers/zip/X7875_NewUnixTest.java
@Test
public void testCentralDirectoryDataIsEmpty() throws Exception {
    xf = new X7875_NewUnix();
    xf.setUID(65535);
    xf.setGID(65535);
    
    byte[] centralData = xf.getCentralDirectoryData();
    assertNotNull("Central directory data should not be null", centralData);
    assertEquals("Central directory data should be empty", 0, centralData.length);
    
    // Verify local data is not empty
    byte[] localData = xf.getLocalFileDataData();
    assertTrue("Local data should have content", localData.length > 0);
}