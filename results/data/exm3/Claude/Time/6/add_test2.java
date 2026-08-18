// org/joda/time/chrono/TestGJDate.java
public void test_cutoverAtYearZero() {
    DateTime cutover = new LocalDate(0, 12, 31, ISOChronology.getInstanceUTC()).toDateTimeAtStartOfDay(DateTimeZone.UTC);
    try {
        GJChronology.getInstance(DateTimeZone.UTC, cutover);
        fail();
    } catch (IllegalArgumentException ex) {
        // expected
    }
}