// org/joda/time/TestDateTimeZone.java
public void testForOffsetHoursMinutesAdditional() {
    assertEquals(DateTimeZone.forID("-00:59"), DateTimeZone.forOffsetHoursMinutes(0, -59));
    assertEquals(DateTimeZone.forID("-23:59"), DateTimeZone.forOffsetHoursMinutes(-23, -59));
}
