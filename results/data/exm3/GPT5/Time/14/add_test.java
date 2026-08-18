// org/joda/time/TestMonthDay_Basics.java::testPlusMonths_int_leapMultipleYears
public void testPlusMonths_int_leapMultipleYears() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(48);
        MonthDay expected = new MonthDay(2, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }