// org/joda/time/chrono/TestGJChronology.java
public void testValidGregorianDateBeforeCutover() {
        // Test a date that is valid in both Gregorian and Julian, before cutover
        DateMidnight dt = new DateMidnight(1500, 1, 15, GJChronology.getInstanceUTC());
        assertEquals(dt.getYear(), 1500);
        assertEquals(dt.getMonthOfYear(), 1);
        assertEquals(dt.getDayOfMonth(), 15);
    }