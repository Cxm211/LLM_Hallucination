// org/joda/time/TestDateTimeZone.java::testForOffsetHoursMinutes_int_int
public void testForOffsetHoursMinutes_additional_invalid_negative24_with_minutes() {
        try {
            DateTimeZone.forOffsetHoursMinutes(-24, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }