// org/joda/time/TestMonthDay_Basics.java
public void testPlusMonths_int_multipleMonthsFromLeap() {
    MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
    MonthDay result = test.plusMonths(12);
    MonthDay expected = new MonthDay(2, 29, ISOChronology.getInstance());
    assertEquals(expected, result);
}