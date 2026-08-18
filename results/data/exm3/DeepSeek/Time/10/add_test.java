// org/joda/time/TestDays.java
public void testFactory_daysBetween_RPartial_MonthDay_Feb29_Mar1() {
        MonthDay start = new MonthDay(2, 29);
        MonthDay end = new MonthDay(3, 1);
        assertEquals(1, Days.daysBetween(start, end).getDays());
        assertEquals(-1, Days.daysBetween(end, start).getDays());
    }
