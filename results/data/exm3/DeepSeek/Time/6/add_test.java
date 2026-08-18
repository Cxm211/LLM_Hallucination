// org/joda/time/chrono/TestGJDate.java
public void test_cutoverPreZeroNonUTC() {
    DateTimeZone zone = DateTimeZone.forID("Europe/London");
    DateTime cutover = new LocalDate(-1, 6, 30, ISOChronology.getInstanceUTC()).toDateTimeAtStartOfDay(DateTimeZone.UTC);
    try {
        GJChronology.getInstance(zone, cutover);
        fail();
    } catch (IllegalArgumentException ex) {
        // expected
    }
}
