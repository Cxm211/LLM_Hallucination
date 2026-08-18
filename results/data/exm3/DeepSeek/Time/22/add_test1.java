// org/joda/time/TestPeriod_Constructors.java
public void testConstructor_long_negativeDuration() throws Throwable {
    DateTimeZone zone = DateTimeZone.getDefault();
    try {
        DateTimeZone.setDefault(DateTimeZone.forOffsetHours(5));
        long length = - (5L * DateTimeConstants.MILLIS_PER_HOUR + 30L * DateTimeConstants.MILLIS_PER_MINUTE);
        Period test = new Period(length);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(-5, test.getHours());
        assertEquals(-30, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    } finally {
        DateTimeZone.setDefault(zone);
    }
}
