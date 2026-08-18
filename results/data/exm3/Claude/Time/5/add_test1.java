// org/joda/time/TestPeriod_Basics.java
public void testNormalizedStandard_periodType_years1() {
    Period test = new Period(3, -40, 0, 0, 0, 0, 0, 0);
    Period result = test.normalizedStandard(PeriodType.yearMonthDay());
    assertEquals(new Period(3, -40, 0, 0, 0, 0, 0, 0), test);
    assertEquals(new Period(0, -4, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay()), result);
}