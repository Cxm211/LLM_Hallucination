// org/apache/commons/compress/archivers/zip/X5455_ExtendedTimestampTest.java::testGettersSetters
@Test
public void testNegativeTimeThrows() {
    try {
        xf.setModifyJavaTime(new Date(-1000L));
        fail("Negative Unix time must be rejected");
    } catch (final IllegalArgumentException expected) {
        // expected
    }
}