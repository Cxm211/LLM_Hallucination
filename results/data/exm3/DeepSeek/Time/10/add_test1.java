// org/joda/time/TestMonths.java
public void testFactory_monthsBetween_RPartial_MonthDay_Feb29_Feb1() {
        MonthDay start = new MonthDay(2, 29);
        MonthDay end = new MonthDay(2, 1);
        assertEquals(0, Months.monthsBetween(start, end).getMonths());
        assertEquals(0, Months.monthsBetween(end, start).getMonths());
    }
