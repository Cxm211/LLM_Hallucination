// org/joda/time/chrono/TestGJDate.java
public void test_cutoverAtYearOne() {
    DateTime cutover = new LocalDate(1, 1, 1, ISOChronology.getInstanceUTC()).toDateTimeAtStartOfDay(DateTimeZone.UTC);
    GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutover);
    assertNotNull(chrono);
}