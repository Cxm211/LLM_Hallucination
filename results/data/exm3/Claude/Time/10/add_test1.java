// org/joda/time/TestMonths.java
public void testFactory_monthsBetween_RPartial_MonthDay_Reversed() {
    MonthDay start = new MonthDay(5, 20);
    MonthDay end = new MonthDay(2, 10);
    
    assertEquals(-3, Months.monthsBetween(start, end).getMonths());
    assertEquals(3, Months.monthsBetween(end, start).getMonths());
}