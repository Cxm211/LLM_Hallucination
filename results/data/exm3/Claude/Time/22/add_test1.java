// org/joda/time/TestDuration_Basics.java
public void testToPeriod_fixedZone_boundary() throws Throwable {
    DateTimeZone zone = DateTimeZone.getDefault();
    try {
        DateTimeZone.setDefault(DateTimeZone.forOffsetHours(2));
        long length = 1L;
        Duration dur = new Duration(length);
        Period test = dur.toPeriod();
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(0, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(1, test.getMillis());
    } finally {
        DateTimeZone.setDefault(zone);
    }
}