// org/joda/time/TestDays.java
public void testFactory_daysBetween_RPartial_MonthDay_Reversed() {
    MonthDay start = new MonthDay(3, 15);
    MonthDay end = new MonthDay(2, 10);
    
    assertEquals(-33, Days.daysBetween(start, end).getDays());
    assertEquals(33, Days.daysBetween(end, start).getDays());
}