// org/joda/time/TestDateTimeZoneCutover.java
public void testDateTimeCreation_london_zeroOffset() {
    DateTimeZone zone = DateTimeZone.forID("Europe/London");
    DateTime base = new DateTime(1970, 1, 1, 1, 0, zone);
    assertEquals("1970-01-01T01:00:00.000Z", base.toString());
}