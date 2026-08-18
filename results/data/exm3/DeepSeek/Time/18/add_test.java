// org/joda/time/chrono/TestGJChronology.java
public void testCutoverGap() {
    // Test a date in the cutover gap (Julian date after cutover).
    try {
        DateMidnight dt = new DateMidnight(1582, 10, 6, GJChronology.getInstanceUTC());
        fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
        assertEquals("Specified date does not exist", e.getMessage());
    }
}
