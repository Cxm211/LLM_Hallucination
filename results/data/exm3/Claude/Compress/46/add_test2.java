// org/apache/commons/compress/archivers/zip/X5455_ExtendedTimestampTest.java
@Test
public void testBoundaryTimestamps() {
    // Test Integer.MIN_VALUE (should work)
    final long minTimeMillis = Integer.MIN_VALUE * 1000L;
    xf.setModifyJavaTime(new Date(minTimeMillis));
    assertEquals(Integer.MIN_VALUE, xf.getModifyTime().getValue());
    
    // Test Integer.MAX_VALUE (should work)
    final long maxTimeMillis = Integer.MAX_VALUE * 1000L;
    xf.setModifyJavaTime(new Date(maxTimeMillis));
    assertEquals(Integer.MAX_VALUE, xf.getModifyTime().getValue());
}