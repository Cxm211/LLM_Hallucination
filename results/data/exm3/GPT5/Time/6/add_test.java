// org/joda/time/chrono/TestGJDate.java::test_cutoverAtYearZero
public void test_cutoverAtYearZero() {
        DateTime cutover = new LocalDate(0, 1, 1, ISOChronology.getInstanceUTC()).toDateTimeAtStartOfDay(DateTimeZone.UTC);
        try {
            GJChronology.getInstance(DateTimeZone.UTC, cutover);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }