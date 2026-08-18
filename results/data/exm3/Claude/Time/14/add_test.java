// org/joda/time/TestMonthDay_Basics.java
public void testPlusDays_int_negativeFromLeap() {
    MonthDay test = new MonthDay(2, 29, ISOChronology.getInstanceUTC());
    MonthDay result = test.plusDays(-1);
    MonthDay expected = new MonthDay(2, 28, ISOChronology.getInstance());
    assertEquals(expected, result);
}