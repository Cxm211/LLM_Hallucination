// org/joda/time/chrono/TestGJChronology.java
public void testInvalidGregorianDateBeforeCutover() {
        // Test a date that is invalid in Gregorian but would be valid in Julian
        // 1500-02-30 does not exist in either calendar
        try {
            new DateMidnight(1500, 2, 30, GJChronology.getInstanceUTC());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }