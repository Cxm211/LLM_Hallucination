// org/joda/time/TestDays.java::testFactory_daysBetween_RPartial_MonthDay
public void testFactory_daysBetween_RPartial_MonthDay_additional_Feb29ToMar1() {
        MonthDay start = new MonthDay(2, 29);
        MonthDay end = new MonthDay(3, 1);
        assertEquals(1, Days.daysBetween(start, end).getDays());
        assertEquals(-1, Days.daysBetween(end, start).getDays());
    }