// org/apache/commons/compress/archivers/zip/X5455_ExtendedTimestampTest.java
@Test
    public void testOutOfRangeBoundaries() {
        // Test value 4294967295 seconds (2^32 - 1), which buggy incorrectly accepts
        try {
            xf.setModifyJavaTime(new Date(4294967295L * 1000L));
            fail("Should throw IllegalArgumentException for 4294967295");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Test value -2147483649 seconds (Integer.MIN_VALUE - 1), which buggy incorrectly accepts
        try {
            xf.setModifyJavaTime(new Date(-2147483649L * 1000L));
            fail("Should throw IllegalArgumentException for -2147483649");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
