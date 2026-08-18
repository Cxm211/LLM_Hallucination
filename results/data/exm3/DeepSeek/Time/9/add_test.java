// org/joda/time/TestDateTimeZone.java
public void testForOffsetHoursMinutesHoursOutOfRange() {
        try {
            DateTimeZone.forOffsetHoursMinutes(24, 1);
            fail("Expected IllegalArgumentException for hours out of range");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            DateTimeZone.forOffsetHoursMinutes(-24, 1);
            fail("Expected IllegalArgumentException for hours out of range");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            DateTimeZone.forOffsetHoursMinutes(100, 0);
            fail("Expected IllegalArgumentException for hours out of range");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }
