// org/apache/commons/compress/archivers/zip/X5455_ExtendedTimestampTest.java
@Test
public void testNegativeTimestampTooSmall() {
    // Test that timestamps smaller than Integer.MIN_VALUE are rejected
    try {
        xf.setModifyJavaTime(new Date(1000L * (Integer.MIN_VALUE - 1L)));
        fail("Time too small for signed 32 bits!");
    } catch (final IllegalArgumentException iae) {
        // Expected
    }
}