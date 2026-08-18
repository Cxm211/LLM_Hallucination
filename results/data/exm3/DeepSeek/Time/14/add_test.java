// org/joda/time/TestMonthDay_Basics.java
public void testPlusMonths_int_fromLeap_multiple() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.plusMonths(2);
        MonthDay expected = new MonthDay(4, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }
