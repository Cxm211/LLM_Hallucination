// org/joda/time/TestDateTimeZone.java
public void testForOffsetHoursMinutes_negativeMinutesWithZeroHours() {
    assertEquals(DateTimeZone.forID("-00:30"), DateTimeZone.forOffsetHoursMinutes(0, -30));
    assertEquals(DateTimeZone.forID("-00:01"), DateTimeZone.forOffsetHoursMinutes(0, -1));
    assertEquals(DateTimeZone.forID("-00:59"), DateTimeZone.forOffsetHoursMinutes(0, -59));
}