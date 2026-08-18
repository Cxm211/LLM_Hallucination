// org/joda/time/TestMonthDay_Basics.java
public void testPlusMonths_int_endOfMonthAdjustToLeap() {
        MonthDay test = new MonthDay(1, 31, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(1);
        MonthDay expected = new MonthDay(2, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }
