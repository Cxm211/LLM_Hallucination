// org/joda/time/TestMonthDay_Basics.java
public void testMinusDays_int_fromLeap() {
    MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
    MonthDay result = test.minusDays(1);
    MonthDay expected = new MonthDay(2, 28, ISOChronology.getInstance());
    assertEquals(expected, result);
}