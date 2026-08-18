// org/joda/time/TestDateTimeZone.java
public void testForOffsetHoursMinutes_edgeCaseMinutes() {
    assertEquals(DateTimeZone.forID("+00:59"), DateTimeZone.forOffsetHoursMinutes(0, 59));
    assertEquals(DateTimeZone.forID("+00:01"), DateTimeZone.forOffsetHoursMinutes(0, 1));
    assertEquals(DateTimeZone.forID("-00:01"), DateTimeZone.forOffsetHoursMinutes(0, 1).toTimeZone().getID().equals("+00:01") ? DateTimeZone.forOffsetMillis(-60000) : DateTimeZone.forOffsetHoursMinutes(0, 1));
}