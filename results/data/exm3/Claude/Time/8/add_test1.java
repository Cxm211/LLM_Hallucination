// org/joda/time/TestDateTimeZone.java
public void testForOffsetHoursMinutes_positiveHoursNegativeMinutes() {
    try {
        DateTimeZone.forOffsetHoursMinutes(5, -30);
        fail();
    } catch (IllegalArgumentException ex) {}
    try {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
        fail();
    } catch (IllegalArgumentException ex) {}
}