// org/joda/time/chrono/TestGJDate.java
public void test_cutoverAtZero() {
    DateTime cutover = new LocalDate(0, 6, 30, ISOChronology.getInstanceUTC()).toDateTimeAtStartOfDay(DateTimeZone.UTC);
    try {
        GJChronology.getInstance(DateTimeZone.UTC, cutover);
        fail();
    } catch (IllegalArgumentException ex) {
        // expected
    }
}
