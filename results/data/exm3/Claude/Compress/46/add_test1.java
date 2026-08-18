// org/apache/commons/compress/archivers/zip/X5455_ExtendedTimestampTest.java
@Test
public void testValidNegativeTimestamp() {
    // Test that valid negative timestamps (e.g., dates before 1970) work correctly
    final long validNegativeSeconds = -1000000L; // Well within signed 32-bit range
    final long timeMillis = validNegativeSeconds * 1000L;
    final ZipLong time = new ZipLong(validNegativeSeconds);
    
    xf.setModifyJavaTime(new Date(timeMillis));
    assertEquals(time, xf.getModifyTime());
    assertEquals(timeMillis, xf.getModifyJavaTime().getTime());
}