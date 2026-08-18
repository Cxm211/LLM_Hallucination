// org/joda/time/TestDateTimeZoneCutover.java
public void testAddWrapFieldInDstChange() {
    DateTime dateTime = new DateTime("2010-10-31T02:59:10.123+02:00", ZONE_PARIS);
    assertEquals("2010-10-31T02:59:10.123+02:00", dateTime.toString());
    DateTime test = dateTime.minuteOfHour().addWrapField(1);
    assertEquals("2010-10-31T02:00:10.123+02:00", test.toString());
}