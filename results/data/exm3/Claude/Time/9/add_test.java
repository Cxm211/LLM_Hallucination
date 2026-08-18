// org/joda/time/TestDateTimeZone.java
public void testForOffsetHoursMinutes_boundaryHours() {
    assertEquals(DateTimeZone.forID("+23:00"), DateTimeZone.forOffsetHoursMinutes(23, 0));
    assertEquals(DateTimeZone.forID("-23:00"), DateTimeZone.forOffsetHoursMinutes(-23, 0));
    try {
        DateTimeZone.forOffsetHoursMinutes(25, 30);
        fail();
    } catch (IllegalArgumentException ex) {}
    try {
        DateTimeZone.forOffsetHoursMinutes(-25, 30);
        fail();
    } catch (IllegalArgumentException ex) {}
}