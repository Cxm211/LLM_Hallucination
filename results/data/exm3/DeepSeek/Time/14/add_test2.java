// org/joda/time/TestMonthDay_Basics.java
public void testMinusMonths_int_fromLeap_multiple() {
        MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
        MonthDay result = test.minusMonths(2);
        MonthDay expected = new MonthDay(12, 29, ISOChronology.getInstance());
        assertEquals(expected, result);
    }
