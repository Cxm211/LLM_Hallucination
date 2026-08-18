// org/joda/time/TestDateTimeZoneCutover.java
public void testRoundFloorInDstChange() {
    DateTime dateTime = new DateTime("2010-10-31T02:30:45.123+02:00", ZONE_PARIS);
    assertEquals("2010-10-31T02:30:45.123+02:00", dateTime.toString());
    DateTime test = dateTime.minuteOfHour().roundFloorCopy();
    assertEquals("2010-10-31T02:30:00.000+02:00", test.toString());
}