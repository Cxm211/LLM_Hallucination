// org/joda/time/TestPeriod_Constructors.java
public void testConstructor_long_dstZone() throws Throwable {
    DateTimeZone zone = DateTimeZone.getDefault();
    try {
        DateTimeZone.setDefault(DateTimeZone.forID("America/New_York"));
        long length = 25L * DateTimeConstants.MILLIS_PER_HOUR;
        Period test = new Period(length);
        assertEquals(PeriodType.standard(), test.getPeriodType());
        assertEquals(0, test.getYears());
        assertEquals(0, test.getMonths());
        assertEquals(0, test.getWeeks());
        assertEquals(0, test.getDays());
        assertEquals(25, test.getHours());
        assertEquals(0, test.getMinutes());
        assertEquals(0, test.getSeconds());
        assertEquals(0, test.getMillis());
    } finally {
        DateTimeZone.setDefault(zone);
    }
}
