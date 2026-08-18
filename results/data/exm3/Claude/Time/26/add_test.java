// org/joda/time/TestDateTimeZoneCutover.java
public void testWithHourOfDayInDstChange_fallBack() {
    DateTime dateTime = new DateTime("2007-11-04T01:30:00.000-04:00", ZONE_NEW_YORK);
    assertEquals("2007-11-04T01:30:00.000-04:00", dateTime.toString());
    DateTime test = dateTime.withHourOfDay(1);
    assertEquals("2007-11-04T01:30:00.000-04:00", test.toString());
}