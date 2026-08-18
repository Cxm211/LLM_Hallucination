// org/joda/time/TestDateTimeZone.java
public void testForOffsetHoursMinutes_boundaryNegativeMinutes() {
    assertEquals(DateTimeZone.forID("-23:59"), DateTimeZone.forOffsetHoursMinutes(-23, -59));
    try {
        DateTimeZone.forOffsetHoursMinutes(0, -60);
        fail();
    } catch (IllegalArgumentException ex) {}
    try {
        DateTimeZone.forOffsetHoursMinutes(-1, -60);
        fail();
    } catch (IllegalArgumentException ex) {}
}